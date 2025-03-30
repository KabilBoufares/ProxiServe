package com.proxiserve.dto;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class PaymentRequestDTO {
    private double amount;
    private String currency;
    private String bookingId;
}
