package com.proxiserve.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingRequest {
    @NotBlank
    private String serviceId;

    @NotNull
    private LocalDateTime bookingDate;

    private double latitude;         // Latitude GPS (optionnel si 0.0)
    private double longitude;        // Longitude GPS (optionnel si 0.0)

    private String description;      // Description des besoins du client (optionnelle)
}
