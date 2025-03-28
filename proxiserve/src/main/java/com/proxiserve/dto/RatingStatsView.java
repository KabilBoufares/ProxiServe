package com.proxiserve.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Map;

@Data
@AllArgsConstructor
public class RatingStatsView {
    private String artisanId;
    private double averageRating;
    private long totalReviews;
    private Map<Integer, Long> ratingDistribution; // ex : {5=3, 4=1}
}
