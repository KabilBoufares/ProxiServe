package com.proxiserve.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour gérer la requête de connexion d'un utilisateur.
 * - Vérifie que l'email est bien formaté.
 * - Vérifie que le mot de passe est non vide et d'une longueur minimale de 6 caractères.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @Email(message = "Format d'email invalide")
    @NotBlank(message = "L'email est requis")
    private String email;

    @NotBlank(message = "Le mot de passe est requis")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;
}
