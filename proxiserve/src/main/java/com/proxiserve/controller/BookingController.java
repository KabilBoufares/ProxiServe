package com.proxiserve.controller;

import com.proxiserve.dto.BookingView;
import com.proxiserve.model.Artisan;
import com.proxiserve.model.Booking;
import com.proxiserve.model.Client;
import com.proxiserve.model.ServiceEntity;
import com.proxiserve.model.User;
import com.proxiserve.repository.BookingRepository;
import com.proxiserve.repository.ClientRepository;
import com.proxiserve.repository.ServiceRepository;
import com.proxiserve.repository.ArtisanRepository;

import com.proxiserve.repository.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    private final BookingRepository bookingRepository;
    private final ClientRepository clientRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final ArtisanRepository artisanRepository;


    public BookingController(BookingRepository bookingRepository,
                            ClientRepository clientRepository,
                            ServiceRepository serviceRepository,
                            UserRepository userRepository,
                            ArtisanRepository artisanRepository) {
        this.bookingRepository = bookingRepository;
        this.clientRepository = clientRepository;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
        this.artisanRepository = artisanRepository;
    }

    //  Créer une réservation (par un client connecté)
    @PostMapping
    public ResponseEntity<?> createBooking(@Valid @RequestBody Booking bookingRequest,
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

        Optional<ServiceEntity> serviceOpt = serviceRepository.findById(bookingRequest.getServiceId());
        if (serviceOpt.isEmpty()) {
            logger.warn("Service non trouvé avec l'ID : {}", bookingRequest.getServiceId());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Service non trouvé");
        }

        bookingRequest.setClientId(clientOpt.get().getId());
        bookingRequest.setCreatedAt(LocalDateTime.now());
        bookingRequest.setStatus("PENDING");

        Booking saved = bookingRepository.save(bookingRequest);
        logger.info("Réservation créée avec ID : {}", saved.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
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

        //  Appliquer un filtre par statut si fourni
        List<Booking> bookings = (status != null && !status.isBlank())
                ? bookingRepository.findByClientIdAndStatus(clientId, status.toUpperCase())
                : bookingRepository.findByClientId(clientId);

        List<BookingView> result = bookings.stream().map(booking -> {
            ServiceEntity service = serviceRepository.findById(booking.getServiceId()).orElse(null);

            return new BookingView(
                booking.getId(),
                booking.getStatus(),
                booking.getBookingDate(),
                booking.getCreatedAt(),
                clientOpt.get().getFullName(),       // On a le client en cache
                clientOpt.get().getEmail(),
                service != null ? service.getTitle() : null,
                service != null ? service.getDescription() : null,
                service != null ? service.getPrice() : 0.0
            );
        }).toList();

        return ResponseEntity.ok(result);
    }


   

    //  Récupérer les réservations des services de l'artisan connecté
    @GetMapping("/artisan")
    public ResponseEntity<?> getBookingsForArtisan(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("[GET] /api/bookings/artisan called by {}", userDetails.getUsername());

        String email = userDetails.getUsername();
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé");
        }

        Optional<Artisan> artisanOpt = artisanRepository.findByUserId(userOpt.get().getId());
        if (artisanOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Artisan non trouvé");
        }

        String artisanId = artisanOpt.get().getId(); //  le vrai ID de l'artisan

        List<ServiceEntity> services = serviceRepository.findByArtisanId(artisanId);
        if (services.isEmpty()) {
            logger.info("Aucun service trouvé pour l'artisan {}", artisanId);
            return ResponseEntity.ok(List.of()); // liste vide mais sans erreur
        }

        List<String> serviceIds = services.stream()
                                        .map(ServiceEntity::getId)
                                        .toList();

        List<Booking> bookings = bookingRepository.findByServiceIdIn(serviceIds);
        

        List<BookingView> result = bookings.stream().map(booking -> {
            String clientId = booking.getClientId();
            String serviceId = booking.getServiceId();

            // Récupérer le client
            var client = clientRepository.findById(clientId).orElse(null);
            // Récupérer le service
            var service = serviceRepository.findById(serviceId).orElse(null);

            return new BookingView(
                booking.getId(),
                booking.getStatus(),
                booking.getBookingDate(),
                booking.getCreatedAt(),
                client != null  ? client.getFullName() : null,
                client != null ? client.getEmail() : null,
                service != null ? service.getTitle() : null,
                service != null ? service.getDescription() : null,
                service != null ? service.getPrice() : 0.0
            );
        }).toList();

        return ResponseEntity.ok(result);

    }


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

        // Vérifie que la réservation concerne un service appartenant à l'artisan connecté
        Optional<ServiceEntity> serviceOpt = serviceRepository.findById(booking.getServiceId());
        if (serviceOpt.isEmpty() || !serviceOpt.get().getArtisanId().equals(artisanOpt.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Action non autorisée");
        }

        booking.setStatus("CONFIRMED");
        bookingRepository.save(booking);

        logger.info("Réservation {} confirmée par l'artisan {}", id, artisanOpt.get().getId());

        return ResponseEntity.ok("Réservation confirmée avec succès");
    }



}
