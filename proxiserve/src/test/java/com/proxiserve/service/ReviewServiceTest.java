package com.proxiserve.service;

import com.proxiserve.dto.RatingStatsView;
import com.proxiserve.model.Review;
import com.proxiserve.repository.ReviewRepository;
import com.proxiserve.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

  

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetRatingStatsForArtisan_withNoReviews() {
        String artisanId = "artisanX";

        when(reviewRepository.findByArtisanId(artisanId)).thenReturn(Collections.emptyList());

        RatingStatsView stats = reviewService.getRatingStatsForArtisan(artisanId);

        assertEquals(artisanId, stats.getArtisanId());
        assertEquals(0.0, stats.getAverageRating());
        assertEquals(0, stats.getTotalReviews());
        assertTrue(stats.getRatingDistribution().isEmpty());
    }

    @Test
    void testGetRatingStatsForArtisan_withMultipleRatings() {
        String artisanId = "artisanY";

        Review review1 = new Review();
        review1.setArtisanId(artisanId);
        review1.setRating(3);

        Review review2 = new Review();
        review2.setArtisanId(artisanId);
        review2.setRating(5);

        when(reviewRepository.findByArtisanId(artisanId)).thenReturn(List.of(review1, review2));

        RatingStatsView stats = reviewService.getRatingStatsForArtisan(artisanId);

        assertEquals(artisanId, stats.getArtisanId());
        assertEquals(4.0, stats.getAverageRating()); // (3 + 5) / 2
        assertEquals(2, stats.getTotalReviews());
        assertEquals(1, stats.getRatingDistribution().get(3));
        assertEquals(1, stats.getRatingDistribution().get(5));
    }

}
