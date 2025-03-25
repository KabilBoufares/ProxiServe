package com.proxiserve.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingView {
    private String id;
    private String status;
    private LocalDateTime bookingDate;
    private LocalDateTime createdAt;

    private String clientFullName;
    private String clientEmail;

    private String serviceTitle;
    private String serviceDescription;
    private double servicePrice;
}
