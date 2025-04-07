package com.proxiserve.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.proxiserve.dto.BookingRequest;
import com.proxiserve.dto.BookingArtisanView;
import com.proxiserve.dto.BookingClientView;
import com.proxiserve.model.Artisan;
import com.proxiserve.model.Booking;
import com.proxiserve.model.Client;
import com.proxiserve.model.Services;
import com.proxiserve.model.User;
import com.proxiserve.repository.BookingRepository;
import com.proxiserve.repository.ClientRepository;
import com.proxiserve.repository.ServiceRepository;
import com.proxiserve.repository.ArtisanRepository;

import com.proxiserve.repository.UserRepository;
import com.proxiserve.service.MailService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    private final BookingRepository bookingRepository;
    private final ClientRepository clientRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final ArtisanRepository artisanRepository;
    private final MailService mailService;

    


    //  Créer une réservation (par un client connecté)
    @PostMapping
    public ResponseEntity<?> createBooking(@Valid @RequestBody BookingRequest bookingRequest,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("[POST] /api/bookings called by {}", userDetails.getUsername());

        String email = userDetails.getUsername();

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            logger.warn("Utilisateur non trouvé pour l'email : {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé");
        }

        User user = userOpt.get();

        Optional<Client> clientOpt = clientRepository.findByUserId(user.getId());
        if (clientOpt.isEmpty()) {
            logger.warn("Client non trouvé pour l'userId : {}", user.getId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Client non trouvé");
        }

        Optional<Services> serviceOpt = serviceRepository.findById(bookingRequest.getServiceId());
        if (serviceOpt.isEmpty()) {
            logger.warn("Service non trouvé avec l'ID : {}", bookingRequest.getServiceId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Service non trouvé");
        }

        Services service = serviceOpt.get();
        String artisanId = service.getArtisanId();

        List<Booking> conflicts = bookingRepository.findByArtisanIdAndBookingDateAndStatus(
                artisanId,
                bookingRequest.getBookingDate(),
                "CONFIRMED"
        );

        if (!conflicts.isEmpty()) {
            logger.warn("Conflit de réservation détecté pour artisan {} à la date {}", artisanId, bookingRequest.getBookingDate());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body("Cet artisan est déjà réservé à cette date.");
        }

        // Construction du nouvel objet Booking
        Booking booking = new Booking();
        booking.setClientId(clientOpt.get().getId());
        booking.setArtisanId(artisanId);
        booking.setServiceId(service.getId());
        booking.setBookingDate(bookingRequest.getBookingDate());
        booking.setCreatedAt(LocalDateTime.now());
        booking.setStatus("PENDING");

        // 🔍 Position GPS si elle est fournie (≠ 0.0)
        if (bookingRequest.getLatitude() != 0.0 && bookingRequest.getLongitude() != 0.0) {
            booking.setLocation(new GeoJsonPoint(bookingRequest.getLongitude(), bookingRequest.getLatitude()));
        }

        // 🔍 Description du besoin
        if (bookingRequest.getDescription() != null && !bookingRequest.getDescription().isBlank()) {
            booking.setDescription(bookingRequest.getDescription());
        }

        // Envoi de mail à l'artisan
        artisanRepository.findById(artisanId).ifPresent(artisan -> {
            String artisanEmail = artisan.getEmail();
            String message = String.format("""
                    Bonjour %s,

                    Vous avez reçu une nouvelle réservation.

                    📅 Date : %s
                    🛠️ Service : %s
                    📝 Description : %s

                    Connectez-vous à votre compte pour confirmer ou rejeter cette demande.

                    -- 
                    L'équipe Proxiserve
                    """,
                    artisan.getProfession(),
                    booking.getBookingDate(),
                    service.getTitle(),
                    booking.getDescription() != null ? booking.getDescription() : "(aucune)"
            );

            mailService.sendEmail(artisanEmail, "📢 Nouvelle réservation reçue !", message);
        });

        Booking saved = bookingRepository.save(booking);

        // 🔁 Renvoyer un BookingArtisanView complet
        Client client = clientOpt.get();
        BookingArtisanView view = new BookingArtisanView(
            saved.getId(),
            saved.getStatus(),
            saved.getBookingDate(),
            saved.getCreatedAt(),
            client.getFullName(),
            user.getEmail(),
            client.getPhoneNumber(),
            service.getTitle(),
            service.getDescription(),
            saved.getLocation()
        );
        logger.info("Réservation créée avec ID : {}", saved.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(view);
    }

   //  Récupérer les réservations du client connecté
@GetMapping("/client")
public ResponseEntity<?> getBookingsForClient(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam(required = false) String status) {

    logger.info("[GET] /api/bookings/client called by {}", userDetails.getUsername());

    Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
    if (userOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé");
    }

    Optional<Client> clientOpt = clientRepository.findByUserId(userOpt.get().getId());
    if (clientOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Client non trouvé");
    }

    String clientId = clientOpt.get().getId();

    // Appliquer un filtre par statut si fourni
    List<Booking> bookings = (status != null && !status.isBlank())
            ? bookingRepository.findByClientIdAndStatus(clientId, status.toUpperCase())
            : bookingRepository.findByClientId(clientId);

    List<BookingClientView> result = bookings.stream().map(booking -> {
        Services service = serviceRepository.findById(booking.getServiceId()).orElse(null);

        return new BookingClientView(
            booking.getId(),
            booking.getStatus(),
            booking.getBookingDate(),
            booking.getCreatedAt(),
            service != null ? service.getTitle() : null,
            service != null ? service.getDescription() : null,
            service != null ? service.getPrice() : 0.0,
            booking.getLocation(),
           
            booking.getDescription() // 💡 Ne pas oublier la description si elle est utile côté client
        );
    }).toList();

    return ResponseEntity.ok(result);
}


   

   //  Récupérer les réservations des services de l'artisan connecté
   @GetMapping("/artisan")
    public ResponseEntity<?> getBookingsForArtisan(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("[GET] /api/bookings/artisan called by {}", userDetails.getUsername());

        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé");
        }

        Optional<Artisan> artisanOpt = artisanRepository.findByUserId(userOpt.get().getId());
        if (artisanOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Artisan non trouvé");
        }

        String artisanId = artisanOpt.get().getId();
        List<Services> services = serviceRepository.findByArtisanId(artisanId);
        if (services.isEmpty()) {
            logger.info("Aucun service trouvé pour l'artisan {}", artisanId);
            return ResponseEntity.ok(List.of());
        }

        List<String> serviceIds = services.stream().map(Services::getId).toList();
        List<Booking> bookings = bookingRepository.findByServiceIdIn(serviceIds);

        List<BookingArtisanView> result = bookings.stream().map(booking -> {
            Client client = clientRepository.findById(booking.getClientId()).orElse(null);
            User clientUser = (client != null) ? userRepository.findById(client.getUserId()).orElse(null) : null;
            Services service = serviceRepository.findById(booking.getServiceId()).orElse(null);

            return new BookingArtisanView(
                booking.getId(),
                booking.getStatus(),
                booking.getBookingDate(),
                booking.getCreatedAt(),
                client != null ? client.getFullName() : null,
                clientUser != null ? clientUser.getEmail() : null, // ✅ mail depuis User
                client != null ? client.getPhoneNumber() : null,
                service != null ? service.getTitle() : null,
                service != null ? service.getDescription() : null,
                booking.getLocation()
            );
        }).toList();

        return ResponseEntity.ok(result);
    }
    //annuler une réservation (par un client connecté)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelBooking(@PathVariable String id,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("[DELETE] /api/bookings/{} demandé par {}", id, userDetails.getUsername());

        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé");
        }

        Optional<Client> clientOpt = clientRepository.findByUserId(userOpt.get().getId());
        if (clientOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Client non trouvé");
        }

        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Réservation non trouvée");
        }

        Booking booking = bookingOpt.get();

        //  Sécurité : vérifier que la réservation appartient bien au client connecté
        if (!booking.getClientId().equals(clientOpt.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Réservation non autorisée");
        }

        if (!"PENDING".equalsIgnoreCase(booking.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("Seules les réservations en attente peuvent être annulées.");
        }
        //  Notification à l'artisan

        artisanRepository.findById(booking.getArtisanId()).ifPresent(artisan -> {
            String subject = "❌ Réservation annulée par le client";
            String body = String.format("""
                Bonjour %s,
        
                Le client a annulé la réservation prévue pour le %s.
                📝 Description : %s
        
                Vous êtes maintenant disponible à ce créneau.
        
                --
                L'équipe Proxiserve
                """,
                artisan.getProfession(),
                booking.getBookingDate(),
                booking.getDescription() != null ? booking.getDescription() : "Non spécifiée"
            );
        
            mailService.sendEmail(artisan.getEmail(), subject, body);
        });

        //  Notification au client
        userRepository.findById(clientOpt.get().getUserId()).ifPresent(clientUser -> {
            String subject = "🔔 Annulation confirmée";
            String body = String.format("""
                Bonjour %s,
    
                Votre réservation prévue pour le %s a été annulée avec succès.
    
                Vous pouvez effectuer une nouvelle réservation à tout moment.
    
                --
                L'équipe Proxiserve
                """,
                clientOpt.get().getFullName(),
                booking.getBookingDate()
            );
    
            mailService.sendEmail(clientUser.getEmail(), subject, body);
        });
        

        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);

        logger.info("Réservation {} annulée avec succès", booking.getId());

        return ResponseEntity.ok("Réservation annulée avec succès");
    }
 
    //  Confirmer une réservation (par un artisan connecté)
    @PutMapping("/{id}/confirm")
    public ResponseEntity<?> confirmBooking(@PathVariable String id,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("[PUT] /api/bookings/{}/confirm demandé par {}", id, userDetails.getUsername());

        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé");
        }

        Optional<Artisan> artisanOpt = artisanRepository.findByUserId(userOpt.get().getId());
        if (artisanOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Artisan non trouvé");
        }

        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Réservation non trouvée");
        }

        Booking booking = bookingOpt.get();

        // Vérifie que l'artisan connecté est bien concerné par cette réservation
        Optional<Services> serviceOpt = serviceRepository.findById(booking.getServiceId());
        if (serviceOpt.isEmpty() || !serviceOpt.get().getArtisanId().equals(artisanOpt.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Action non autorisée");
        }

        // Mise à jour du statut
        booking.setStatus("CONFIRMED");
        bookingRepository.save(booking);
        logger.info("Réservation {} confirmée par artisan {}", id, artisanOpt.get().getId());

        // 🔔 Notification au client
        clientRepository.findById(booking.getClientId()).ifPresent(client -> {
            userRepository.findById(client.getUserId()).ifPresent(user -> {
                String subject = "✅ Votre réservation a été confirmée !";
                String body = String.format("""
                    Bonjour %s,

                    L'artisan %s a confirmé votre réservation prévue pour le %s.

                    📝 Description : %s

                    Merci pour votre confiance.

                    --
                    L'équipe Proxiserve
                    """,
                    client.getFullName(),
                    artisanOpt.get().getProfession(),
                    booking.getBookingDate(),
                    booking.getDescription() != null ? booking.getDescription() : "Non spécifiée"
                );

                mailService.sendEmail(user.getEmail(), subject, body);
            });
        });

        return ResponseEntity.ok("Réservation confirmée avec succès");
    }


    //  Refuser une réservation (par un artisan connecté)
    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectBooking(@PathVariable String id,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("[PUT] /api/bookings/{}/reject demandé par {}", id, userDetails.getUsername());

        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé");
        }

        Optional<Artisan> artisanOpt = artisanRepository.findByUserId(userOpt.get().getId());
        if (artisanOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Artisan non trouvé");
        }

        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Réservation non trouvée");
        }

        Booking booking = bookingOpt.get();

        // Vérifie que la réservation concerne un service appartenant à l'artisan connecté
        Optional<Services> serviceOpt = serviceRepository.findById(booking.getServiceId());
        if (serviceOpt.isEmpty() || !serviceOpt.get().getArtisanId().equals(artisanOpt.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Action non autorisée");
        }
        booking.setStatus("REJECTED");

        clientRepository.findById(booking.getClientId()).ifPresent(client -> {
            userRepository.findById(client.getUserId()).ifPresent(user -> {
                String subject = "❌ Réservation rejetée";
                String body = String.format("""
                    Bonjour %s,
    
                    Nous sommes désolés, l'artisan %s a rejeté votre réservation.
    
                    Vous pouvez réserver un autre professionnel via Proxiserve.
    
                    --
                    L'équipe Proxiserve
                    """,
                    client.getFullName(),
                    artisanOpt.get().getProfession()
                );
    
                mailService.sendEmail(user.getEmail(), subject, body);
            });
        });

        
        bookingRepository.save(booking);

        logger.info("Réservation {} rejetée par l'artisan {}", id, artisanOpt.get().getId());

        return ResponseEntity.ok("Réservation rejetée avec succès");
    }

    //  Marquer une réservation comme terminée (par un artisan connecté)
    @PutMapping("/{id}/complete")
    public ResponseEntity<?> completeBooking(@PathVariable String id,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("[PUT] /api/bookings/{}/complete demandé par {}", id, userDetails.getUsername());

        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé");

        Optional<Artisan> artisanOpt = artisanRepository.findByUserId(userOpt.get().getId());
        if (artisanOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Artisan non trouvé");

        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Réservation non trouvée");

        Booking booking = bookingOpt.get();
        Optional<Services> serviceOpt = serviceRepository.findById(booking.getServiceId());
        if (serviceOpt.isEmpty() || !serviceOpt.get().getArtisanId().equals(artisanOpt.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Action non autorisée");
        }

        booking.setStatus("COMPLETED");
        clientRepository.findById(booking.getClientId()).ifPresent(client -> {
            userRepository.findById(client.getUserId()).ifPresent(user -> {
                String subject = "🎉 Réservation terminée avec succès";
                String body = String.format("""
                    Bonjour %s,
        
                    L'artisan %s a indiqué que votre réservation est maintenant terminée.
        
                    Nous espérons que vous êtes satisfait(e) du service.
        
                    N'hésitez pas à laisser un avis ⭐⭐⭐⭐⭐ !
        
                    --
                    L'équipe Proxiserve
                    """,
                    client.getFullName(),
                    artisanOpt.get().getProfession()
                );
        
                mailService.sendEmail(user.getEmail(), subject, body);
            });
        });
        
        bookingRepository.save(booking);

        logger.info("Réservation {} marquée comme terminée par l'artisan {}", id, artisanOpt.get().getId());
        return ResponseEntity.ok("Réservation terminée avec succès");
    }

}
