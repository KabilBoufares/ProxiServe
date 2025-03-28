package com.proxiserve.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour gérer l'inscription des utilisateurs (clients et artisans).
 * - Vérifie que les champs obligatoires sont bien remplis.
 * - Applique des contraintes de validation sur certains champs sensibles.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

    /** Email de l'utilisateur (doit être valide et non vide) */
    @NotBlank(message = "L'email ne peut pas être vide")
    @Email(message = "Format d'email invalide")
    private String email;

    /** Mot de passe sécurisé (min 6 caractères) */
    @NotBlank(message = "Le mot de passe est requis")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;

    /** Rôle de l'utilisateur (CLIENT ou ARTISAN) */
    @NotBlank(message = "Le rôle ne peut pas être vide")
    private String role;

    // Champs spécifiques aux clients
    private String fullName;

    /** Numéro de téléphone valide (ex: +33123456789) */
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,15}$", message = "Numéro de téléphone invalide")
    private String phoneNumber;

    // Champs spécifiques aux artisans
    private String profession;
    private String companyName;
    private List<String> serviceCategories;
    @NotNull(message = "Longitude requise pour les artisans")
    private Double latitude;
    @NotNull(message = "Longitude requise pour les artisans")
    private Double longitude;
}
