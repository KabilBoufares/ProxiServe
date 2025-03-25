package com.proxiserve.repository;

import com.proxiserve.model.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BookingRepository extends MongoRepository<Booking, String> {
    List<Booking> findByClientId(String clientId);
    List<Booking> findByArtisanId(String artisanId);
    List<Booking> findByServiceIdIn(List<String> serviceIds);
    List<Booking> findByClientIdAndStatus(String clientId, String status);
    


}