package com.proxiserve.repository;

import com.proxiserve.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends MongoRepository<Booking, String> {
    List<Booking> findByClientId(String clientId);
    List<Booking> findByArtisanId(String artisanId);
    List<Booking> findByServiceIdIn(List<String> serviceIds);
    List<Booking> findByClientIdAndStatus(String clientId, String status);
    List<Booking> findByArtisanIdAndBookingDateAndStatus(String artisanId, LocalDateTime bookingDate, String status);

    


}