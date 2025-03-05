package com.proxiserve.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.proxiserve.model.User;
import com.proxiserve.repository.UserRepository;

import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(User user) {
        // Vérifier si l'email existe déjà
        Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser.isPresent()) {
            logger.warn("⚠️ Tentative d'inscription avec un email déjà utilisé : {}", user.getEmail());
            throw new IllegalStateException("Error: Email is already in use!");
        }

        // Encodage du mot de passe
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Sauvegarde de l'utilisateur
        User savedUser = userRepository.save(user);
        logger.info("✅ Utilisateur enregistré avec succès : {}", savedUser.getEmail());

        // Masquer le mot de passe avant de retourner l'objet
        savedUser.setPassword("********");  // Masquer le mot de passe
        return savedUser;
    }
}
