package com.proxiserve.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * Modèle représentant un avis laissé par un utilisateur sur un artisan.
 */
@Document(collection = "reviews") // Stocke les avis dans une collection séparée
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    /** Identifiant unique de l'avis */
    @Id
    private String id;

    /** Identifiant de l'utilisateur ayant laissé l'avis */
    @NotBlank(message = "L'ID de l'utilisateur est requis")
    private String userId;

    /** Identifiant de l'artisan évalué */
    @NotBlank(message = "L'ID de l'artisan est requis")
    @Indexed // Accélère les recherches d'avis pour un artisan
    private String artisanId;

    /** Note attribuée à l'artisan (entre 1 et 5) */
    @Min(1)
    @Max(5)
    @NotNull(message = "La note est obligatoire")
    private Integer rating;

    /** Commentaire sur l'artisan (optionnel, max 500 caractères) */
    @Size(max = 500, message = "Le commentaire ne peut pas dépasser 500 caractères")
    private String comment;

    /** Date de création de l'avis (gérée automatiquement par MongoDB) */
    @CreatedDate
    private LocalDateTime createdAt = LocalDateTime.now();
}
