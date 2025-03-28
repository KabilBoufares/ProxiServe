package com.proxiserve.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "bookings")
public class Booking {
    @Id
    private String id;
    @DBRef 
    private Client client;        // Client ayant effectué la réservation
    @DBRef
    private ServiceEntity service; // Service réservé
    private String clientId;       // ID du client ayant effectué la réservation
    private String artisanId;      // ID de l'artisan concerné
    private String serviceId;      // ID du service réservé

    private LocalDateTime bookingDate;   // Date à laquelle le service est demandé
    private String status;               // Status : PENDING, CONFIRMED, CANCELLED, COMPLETED
    private String paymentStatus = "UNPAID"; // UNPAID ou PAID
    private String paymentMethod;            // paypal, cash, stripe...
    private boolean paymentCompleted = false;

    private LocalDateTime createdAt = LocalDateTime.now(); // Date de création
}
