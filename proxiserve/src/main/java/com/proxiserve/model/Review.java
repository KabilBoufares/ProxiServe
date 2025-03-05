package com.proxiserve.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Document(collection = "reviews") // Stocke les avis dans une collection séparée
@Data
@NoArgsConstructor
public class Review {

    @Id
    private String id;

    @NotBlank(message = "User ID cannot be blank")
    private String userId;  // L'utilisateur qui a laissé l'avis

    @NotBlank(message = "Artisan ID cannot be blank")
    private String artisanId; // L'artisan auquel l'avis est attribué

    @Min(1)
    @Max(5)
    @NotNull(message = "Rating is required")
    private Integer rating;  // Note entre 1 et 5

    private String comment;  // Commentaire facultatif

    private LocalDateTime createdAt = LocalDateTime.now(); // Date de l'avis

    // Constructeur utile
    public Review(String userId, String artisanId, Integer rating, String comment) {
        this.userId = userId;
        this.artisanId = artisanId;
        this.rating = rating;
        this.comment = comment;
    }
}
