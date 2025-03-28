package com.proxiserve.service;

import com.proxiserve.dto.RatingStatsView;
import com.proxiserve.model.Review;
import com.proxiserve.repository.ReviewRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    public RatingStatsView getRatingStatsForArtisan(String artisanId) {
        List<Review> reviews = reviewRepository.findByArtisanId(artisanId);

        long total = reviews.size();

        double average = total == 0 ? 0.0 :
                reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);

        Map<Integer, Long> distribution = reviews.stream()
                .collect(Collectors.groupingBy(Review::getRating, Collectors.counting()));

        return new RatingStatsView(artisanId, average, total, distribution);
    }
}
