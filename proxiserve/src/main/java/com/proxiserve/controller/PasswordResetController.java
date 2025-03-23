package com.proxiserve.controller;

import com.proxiserve.model.User;
import com.proxiserve.repository.UserRepository;
import com.proxiserve.security.jwt.JwtTokenProvider;
import com.proxiserve.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {

    private static final Logger logger = LoggerFactory.getLogger(PasswordResetController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MailService mailService;

    /**
     * 🔹 **Étape 1** : Demande de réinitialisation de mot de passe.
     * - Vérifie si l'email existe dans la base.
     * - Génère un token temporaire sécurisé (validité : 15 minutes).
     * - Stocke le token en base pour empêcher les réutilisations frauduleuses.
     * - Envoie un email contenant un lien de réinitialisation avec le token.
     */
    @PostMapping("/request-reset-password")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> request) {

        String email = request.get("email");

        if (email == null || email.isEmpty()) {
            logger.warn(" Tentative de demande de reset sans email !");
            return ResponseEntity.badRequest().body("L'email est requis !");
        }

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            logger.warn(" Tentative de reset avec un email inconnu : {}", email);
            return ResponseEntity.ok(Map.of("message", "Si cet email existe, un lien de réinitialisation sera envoyé."));
        }

        User user = userOpt.get();

        // Générer un token temporaire (valide 15 minutes)
        String token = jwtTokenProvider.generateTokenWithExpiration(email, 15 * 60 * 1000);
        user.setResetPasswordToken(token);
        user.setTokenExpiration(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        // Envoyer l'email avec le lien de réinitialisation
        String resetLink = "https://mon-site.com/reset-password?token=" + token;
        String emailContent = "Bonjour,\n\nCliquez sur ce lien pour réinitialiser votre mot de passe :\n" 
                              + resetLink + 
                              "\n\n Ce lien est valable 15 minutes.";
        mailService.sendEmail(email, "Réinitialisation de mot de passe", emailContent);

        logger.info("📩 Email de réinitialisation envoyé à {}", email);
        return ResponseEntity.ok(Map.of("message", "Si cet email existe, un lien de réinitialisation a été envoyé."));
    }

    /**
     * 🔹 **Étape 2** : Réinitialisation du mot de passe.
     * - Vérifie la validité du token et sa non-expiration.
     * - Vérifie la complexité du nouveau mot de passe.
     * - Met à jour le mot de passe de manière sécurisée.
     * - Supprime immédiatement le token pour éviter toute réutilisation.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {

        String token = request.get("token");
        String newPassword = request.get("newPassword");

        // Vérifications de base
        if (token == null || token.isEmpty()) {
            logger.warn(" Réinitialisation refusée : Token absent");
            return ResponseEntity.badRequest().body("Le token est requis !");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            logger.warn(" Réinitialisation refusée : Nouveau mot de passe absent");
            return ResponseEntity.badRequest().body("Le nouveau mot de passe est requis !");
        }

        // Vérifier la validité du token
        if (!jwtTokenProvider.validateToken(token)) {
            logger.warn(" Token invalide ou expiré !");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide ou expiré !");
        }

        // Récupérer l'email depuis le token
        String email = jwtTokenProvider.getUserEmailFromToken(token);
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            logger.warn(" Utilisateur introuvable pour le token fourni !");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide !");
        }

        User user = userOpt.get();

        // Vérifier si le token en base correspond à celui fourni et s'il est encore valide
        if (!token.equals(user.getResetPasswordToken()) || LocalDateTime.now().isAfter(user.getTokenExpiration())) {
            logger.warn(" Token expiré ou non valide !");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide ou expiré !");
        }

        // Vérifier la robustesse du mot de passe
        if (!isPasswordValid(newPassword)) {
            logger.warn(" Mot de passe trop faible !");
            return ResponseEntity.badRequest().body("Le mot de passe doit contenir au moins 8 caractères, une majuscule, un chiffre et un caractère spécial !");
        }

        // Mettre à jour le mot de passe et supprimer le token
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setTokenExpiration(null);
        userRepository.save(user);

        logger.info(" Mot de passe mis à jour avec succès pour l'utilisateur : {}", email);
        return ResponseEntity.ok("Mot de passe mis à jour avec succès !");
    }

    /**
     * Vérifie si un mot de passe est conforme aux règles de sécurité.
     * - Minimum 8 caractères
     * - Au moins 1 majuscule
     * - Au moins 1 chiffre
     * - Au moins 1 caractère spécial
     */
    private boolean isPasswordValid(String password) {
        return password.length() >= 8 &&
               password.matches(".*[A-Z].*") &&
               password.matches(".*[0-9].*") &&
               password.matches(".*[!@#$%^&*(),.?\":{}|<>].*");
    }
}
