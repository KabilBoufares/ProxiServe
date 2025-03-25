package com.proxiserve.controller;

import com.proxiserve.model.Booking;
import com.proxiserve.model.Client;
import com.proxiserve.model.ServiceEntity;
import com.proxiserve.model.User;
import com.proxiserve.repository.BookingRepository;
import com.proxiserve.repository.ClientRepository;
import com.proxiserve.repository.ServiceRepository;
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

    public BookingController(BookingRepository bookingRepository,
                             ClientRepository clientRepository,
                             ServiceRepository serviceRepository,
                             UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.clientRepository = clientRepository;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
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
    public ResponseEntity<?> getBookingsForClient(@AuthenticationPrincipal UserDetails userDetails) {
        logger.info("[GET] /api/bookings/client called by {}", userDetails.getUsername());

        String email = userDetails.getUsername();
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé");
        }

        Optional<Client> clientOpt = clientRepository.findByUserId(userOpt.get().getId());
        if (clientOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Client non trouvé");
        }

        List<Booking> bookings = bookingRepository.findByClientId(clientOpt.get().getId());
        return ResponseEntity.ok(bookings);
    }

    //  Obtenir une réservation par son ID
    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable String id) {
        logger.info("[GET] /api/bookings/{}", id);
        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        return bookingOpt
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    //  Annuler une réservation
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelBooking(@PathVariable String id,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("[DELETE] /api/bookings/{} demandé par {}", id, userDetails.getUsername());

        Optional<Booking> bookingOpt = bookingRepository.findById(id);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Réservation non trouvée");
        }

        Booking booking = bookingOpt.get();
        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);

        return ResponseEntity.ok("Réservation annulée avec succès");
    }
}
