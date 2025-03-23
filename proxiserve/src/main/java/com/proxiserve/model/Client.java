package com.proxiserve.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * Modèle représentant un client dans la plateforme.
 */
@Document(collection = "clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    /** Identifiant unique généré par MongoDB */
    @Id
    private String id;

    /** Référence à l'utilisateur associé (User) */
    @NotBlank(message = "L'ID utilisateur ne peut pas être vide")
    private String userId;

    /** Nom complet du client */
    @NotBlank(message = "Le nom du client est requis")
    private String fullName;

    /** Numéro de téléphone (validation au format international) */
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,15}$", message = "Numéro de téléphone invalide")
    private String phoneNumber;

    /** Date de création du client (ajoutée automatiquement) */
    @CreatedDate
    private LocalDateTime createdAt = LocalDateTime.now();
}
