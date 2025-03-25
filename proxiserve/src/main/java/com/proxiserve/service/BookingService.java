package com.proxiserve.service;

import com.proxiserve.dto.BookingRequest;
import com.proxiserve.model.Artisan;
import com.proxiserve.model.Booking;
import com.proxiserve.model.ServiceEntity;
import com.proxiserve.repository.ArtisanRepository;
import com.proxiserve.repository.BookingRepository;
import com.proxiserve.repository.ServiceRepository;
import com.proxiserve.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ServiceRepository serviceRepository;
    private final ArtisanRepository artisanRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository, ServiceRepository serviceRepository,
                          ArtisanRepository artisanRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.serviceRepository = serviceRepository;
        this.artisanRepository = artisanRepository;
        this.userRepository = userRepository;
    }

    public Booking createBooking(BookingRequest request, Principal principal) {
        String email = principal.getName();

        // Vérifie que l'utilisateur est bien un client existant
        var client = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Client non trouvé"));

        var service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service non trouvé"));

        var artisan = artisanRepository.findById(service.getArtisanId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artisan non trouvé"));

        Booking booking = new Booking();
        booking.setClientId(client.getId());
        booking.setServiceId(service.getId());
        booking.setArtisanId(artisan.getId());
        booking.setBookingDate(request.getBookingDate());
        booking.setStatus("PENDING");

        return bookingRepository.save(booking);
    }
}
