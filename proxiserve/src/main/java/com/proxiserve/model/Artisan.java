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
    private String userId;


    private String email;


    private String phoneNumber;

    private String profilePictureUrl;
    private String biography;
    private List<String> skills;


    private String profession;

    /** Nom de l'entreprise (optionnel) */
    private String companyName;


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
