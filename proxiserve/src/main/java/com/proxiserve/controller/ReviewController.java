package com.proxiserve.controller;

import com.proxiserve.model.Review;
import com.proxiserve.repository.ReviewRepository;
import com.proxiserve.repository.UserRepository;
import com.proxiserve.service.ReviewService;
import com.proxiserve.model.User;
import com.proxiserve.dto.RatingStatsView;
import com.proxiserve.dto.ReviewView;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    @Autowired
    private ReviewService reviewService;

    public ReviewController(ReviewRepository reviewRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    // ✅ Ajouter un avis
    @PostMapping
    public ResponseEntity<?> addReview(@RequestBody @Valid Review review,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

        review.setUserId(user.getId());
        review.setCreatedAt(LocalDateTime.now());

        Review saved = reviewRepository.save(review);
        return ResponseEntity.ok(saved);
    }

    // ✅ Voir les avis d’un artisan (ReviewView)
    @GetMapping("/artisan/{artisanId}")
    public ResponseEntity<List<ReviewView>> getReviewsByArtisan(@PathVariable String artisanId) {
        List<Review> reviews = reviewRepository.findByArtisanId(artisanId);

        List<ReviewView> reviewViews = reviews.stream().map(review -> {
            String clientName = userRepository.findById(review.getUserId())
                    .map(User::getFullName)
                    .orElse("Client inconnu");

            return new ReviewView(
                    clientName,
                    review.getRating(),
                    review.getComment(),
                    review.getCreatedAt()
            );
        }).toList();

        return ResponseEntity.ok(reviewViews);
    }

    // ✅ Supprimer un avis (par le client ou l'admin)
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable String reviewId,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
        if (reviewOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Avis non trouvé");
        }

        Optional<User> userOpt = userRepository.findByEmail(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
        }

        Review review = reviewOpt.get();
        if (!review.getUserId().equals(userOpt.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Non autorisé à supprimer cet avis.");
        }

        reviewRepository.deleteById(reviewId);
        return ResponseEntity.ok("Avis supprimé avec succès.");
    }

    // ✅ Statistiques d’un artisan : moyenne, nb d’avis, etc.
    @GetMapping("/stats/{artisanId}")
    public ResponseEntity<RatingStatsView> getStatsForArtisan(@PathVariable String artisanId) {
        RatingStatsView stats = reviewService.getRatingStatsForArtisan(artisanId);
        return ResponseEntity.ok(stats);
    }
}
