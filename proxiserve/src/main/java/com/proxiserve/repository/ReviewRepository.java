package com.proxiserve.repository;

import com.proxiserve.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends MongoRepository<Review, String> {

    //  Trouver tous les avis d’un artisan
    List<Review> findByArtisanId(String artisanId);

    //  Vérifier si un avis existe déjà pour une réservation
    boolean existsByBookingId(String bookingId);

    // (optionnel) récupérer tous les avis d’un utilisateur
    List<Review> findByUserId(String userId);

    // (optionnel) pour afficher un seul avis si besoin

   
    Optional<Review> findByBookingId(String bookingId);
}
