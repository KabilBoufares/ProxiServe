package com.proxiserve.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Modèle représentant un artisan dans la plateforme.
 */
@Document(collection = "artisans")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Artisan {

    /** Identifiant unique généré par MongoDB */
    @Id
    private String id;

    /** Référence à l'utilisateur associé (User) */
    @NotBlank(message = "L'ID utilisateur ne peut pas être vide")
    private String userId;

    /** Profession de l'artisan */
    @NotBlank(message = "La profession est requise")
    private String profession;

    /** Nom de l'entreprise (optionnel) */
    private String companyName;

    /** Catégories de services proposés par l'artisan */
    @NotNull(message = "Les catégories de service ne peuvent pas être nulles")
    @Size(min = 1, message = "L'artisan doit proposer au moins un service")
    private List<String> serviceCategories;

    /** Liste des avis laissés par les clients */
    private List<Review> reviews;

    /** Localisation géographique pour la recherche de proximité */
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint location;

    /** Date de création de l'artisan */
    @CreatedDate
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Calcule dynamiquement la note moyenne des avis de l'artisan.
     * @return Moyenne des notes ou 0.0 si aucun avis n'est disponible.
     */
    public double getRating() {
        return (reviews != null && !reviews.isEmpty()) 
            ? reviews.stream().mapToDouble(Review::getRating).average().orElse(0.0) 
            : 0.0;
    }
}
