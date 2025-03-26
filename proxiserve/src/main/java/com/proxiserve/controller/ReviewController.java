package com.proxiserve.controller;

import java.util.ArrayList;

import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proxiserve.model.Artisan;
import com.proxiserve.model.Review;
import com.proxiserve.repository.ArtisanRepository;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ArtisanRepository artisanRepository;

    public ReviewController(ArtisanRepository artisanRepository) {
        this.artisanRepository = artisanRepository;
    }

    @PostMapping
    public ResponseEntity<String> addReview(@RequestBody Review review) {
        Optional<Artisan> artisanOpt = artisanRepository.findById(review.getArtisanId());
        if (artisanOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Artisan introuvable");
        }

        Artisan artisan = artisanOpt.get();
        if (artisan.getReviews() == null) {
            artisan.setReviews(new ArrayList<>());
        }

        artisan.getReviews().add(review);
        artisanRepository.save(artisan);

        return ResponseEntity.ok("Avis ajouté avec succès");
    }
}


