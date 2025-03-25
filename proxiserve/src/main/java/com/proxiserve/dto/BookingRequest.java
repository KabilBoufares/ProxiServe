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
}
