package com.proxiserve.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

/**
 * Modèle représentant un artisan dans la plateforme.
 */
@Document(collection = "artisans")
@Data
public class Artisan {

    /** Identifiant unique généré par MongoDB */
    @Id
    private String id;

    /** Référence à l'utilisateur associé (User) */
    @NotBlank(message = "L'ID utilisateur ne peut pas être vide")
    private String userId;

    /*Email */
    @NotBlank(message = "Le nom complet ne peut pas être vide")
    private String email;

    /** Numéro de téléphone de l'artisan */
    @NotBlank(message = "Le numéro de téléphone est requis")
    private String phoneNumber;

    private String profilePictureUrl;
    private String biography;
    private List<String> skills;

    /** Profession de l'artisan */
    @NotBlank(message = "La profession est requise")
    private String profession;

    /** Nom de l'entreprise (optionnel) */
    private String companyName;

    /** Catégories de services proposés par l'artisan */
    @NotNull(message = "Les catégories de service ne peuvent pas être nulles")
    @Size(min = 1, message = "L'artisan doit proposer au moins un service")
    private List<String> serviceCategories;


   

    private String workingHoursWeekdays; // ex : "9:00 - 18:00"
    private String workingHoursSaturday;
    private String workingHoursSunday;



    /** Localisation géographique pour la recherche de proximité */
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint location;

    /** Date de création de l'artisan */
    @CreatedDate
    private LocalDateTime createdAt = LocalDateTime.now();

    @org.springframework.data.annotation.Transient
    private Double averageRating;


    private List<String> workPhotoUrls = new ArrayList<>();



    
}
