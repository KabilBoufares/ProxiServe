package com.proxiserve.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Modèle utilisateur pour l'authentification et la gestion des rôles.
 * Implémente {@link UserDetails} pour l'intégration avec Spring Security.
 */
@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    /** Identifiant unique généré par MongoDB */
    @Id
    private String id;

    /** Email unique, utilisé pour l'authentification */
    @Indexed(unique = true)
    @NotBlank(message = "L'email ne peut pas être vide")
    @Email(message = "Format d'email invalide")
    private String email;

    /** Mot de passe sécurisé (encodé avec BCrypt) */
    @NotBlank(message = "Le mot de passe est requis")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;

    /** Rôle de l'utilisateur (ex: ROLE_CLIENT, ROLE_ADMIN) */
    @NotBlank(message = "Le rôle est requis")
    private String role;

    /** Date de création de l'utilisateur (gérée automatiquement) */
    @CreatedDate
    private LocalDateTime createdAt = LocalDateTime.now();

    /** Nombre d'échecs de connexion pour la gestion du verrouillage de compte */
    private int failedLoginAttempts = 0;

    /** Indique si le compte est verrouillé après trop de tentatives infructueuses */
    private boolean accountLocked = false;

    /** Heure à laquelle le compte a été verrouillé */
    private LocalDateTime lockTime;

    /** Token de réinitialisation du mot de passe */
    private String resetPasswordToken;

    /** Date d'expiration du token de réinitialisation */
    private LocalDateTime tokenExpiration;

    /**
     * Retourne les autorisations de l'utilisateur en fonction de son rôle.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role != null ? List.of(new SimpleGrantedAuthority(role.trim())) : Collections.emptyList();
    }

    /**
     * Retourne l'email en tant que nom d'utilisateur pour Spring Security.
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Indique si le compte est actif (expiré = false).
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indique si le compte est verrouillé.
     */
    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    /**
     * Indique si les informations d'identification (mot de passe) sont expirées.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indique si l'utilisateur est activé.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    
}
