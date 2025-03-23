package com.proxiserve.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.proxiserve.model.User;
import com.proxiserve.repository.UserRepository;

import java.util.Optional;

/**
 * Service pour la gestion des utilisateurs (inscription, récupération).
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Enregistre un nouvel utilisateur après vérifications.
     * @param user Utilisateur à enregistrer.
     * @return L'utilisateur enregistré (avec mot de passe masqué).
     * @throws IllegalArgumentException si l'email est déjà utilisé ou si le mot de passe est invalide.
     */
    @Transactional
    public User registerUser(User user) {
        // Vérifier si l'email existe déjà
        if (userRepository.existsByEmail(user.getEmail())) {
            logger.warn(" Tentative d'inscription avec un email déjà utilisé : {}", user.getEmail());
            throw new IllegalArgumentException("Erreur : Cet email est déjà utilisé !");
        }

        // Vérifier la validité du mot de passe
        validatePassword(user.getPassword());

        // Assigner un rôle par défaut si non spécifié
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("ROLE_CLIENT");  
        }

        // Encodage du mot de passe
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Sauvegarde de l'utilisateur
        User savedUser = userRepository.save(user);
        logger.info(" Utilisateur enregistré avec succès : {}", savedUser.getEmail());

        // Masquer le mot de passe avant de retourner l'objet
        savedUser.setPassword("********");
        return savedUser;
    }

    /**
     * Valide la robustesse du mot de passe.
     * @param password Mot de passe à valider.
     * @throws IllegalArgumentException si le mot de passe ne respecte pas les critères.
     */
    private void validatePassword(String password) {
        if (password == null || password.length() < 8 ||
            !password.matches(".*[A-Z].*") ||
            !password.matches(".*[0-9].*")) {
            logger.error(" Mot de passe non sécurisé !");
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères, une majuscule et un chiffre !");
        }
    }
}
