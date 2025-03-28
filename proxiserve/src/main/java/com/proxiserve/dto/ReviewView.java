package com.proxiserve.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ReviewView {

    private String clientName;         // nom complet du client
    private int rating;                // note (1 à 5)
    private String comment;            // commentaire libre
    private LocalDateTime createdAt;   // date d’ajout de l’avis
}
