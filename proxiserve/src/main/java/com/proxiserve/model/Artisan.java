package com.proxiserve.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "artisans")
@Data
@NoArgsConstructor
public class Artisan {

    @Id
    private String id;

    @NotBlank(message = "User ID cannot be blank")
    @Indexed(unique = true)  // Supprimer cette contrainte si plusieurs artisans peuvent être liés à un même utilisateur
    private String userId; // Référence au User

    @NotBlank(message = "Profession cannot be blank")
    private String profession;

    private String companyName;
    
    @NotNull(message = "Service categories cannot be null")
    private List<String> serviceCategories;

    //@DBRef  // Option si `Review` est dans une collection séparée
    private List<Review> reviews;

    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE) // Index géospatial pour la recherche par localisation
    private GeoJsonPoint location;

    // Calculer dynamiquement le rating au lieu de le stocker
    public double getRating() {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }
        return reviews.stream().mapToDouble(Review::getRating).average().orElse(0.0);
    }
}
