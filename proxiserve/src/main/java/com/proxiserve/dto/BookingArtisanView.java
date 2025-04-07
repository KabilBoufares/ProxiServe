package com.proxiserve.dto;

import java.time.LocalDateTime;

import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingArtisanView {
    private String id;
    private String status;
    private LocalDateTime bookingDate;
    private LocalDateTime createdAt;

    private String clientFullName;
    private String clientEmail;
    private String clientPhoneNumber;

    private String serviceTitle;
    private String serviceDescription;

    private GeoJsonPoint location; // Position GPS (longitude, latitude)
}
