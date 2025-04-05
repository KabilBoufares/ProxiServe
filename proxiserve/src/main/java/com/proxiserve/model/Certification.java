package com.proxiserve.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Data
@Document(collection = "certifications")
public class Certification {

    @Id
    private String id;
    @Indexed
    private String artisanId; // lien vers l'artisan

    private String name;
    private String organization;
    private String dateObtained; // String ou LocalDate selon ton choix
    private String description;
}
