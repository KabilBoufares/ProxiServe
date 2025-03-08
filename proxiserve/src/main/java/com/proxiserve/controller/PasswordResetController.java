package com.proxiserve.controller;

import com.proxiserve.model.User;
import com.proxiserve.repository.UserRepository;
import com.proxiserve.security.jwt.JwtTokenProvider;
import com.proxiserve.service.MailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private MailService mailService;

    /**
     *  Étape 1 : Demande de réinitialisation de mot de passe.
     * 
     * - Vérifie si l'email existe dans la base de données.
     * - Génère un token temporaire valide pour 15 minutes.
     * - Stocke le token en base pour éviter les attaques.
     * - Envoie un email avec un lien de réinitialisation contenant le token.
     */
    @PostMapping("/request-reset-password")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> request) {

        // Récupération de l'email depuis la requête
        String email = request.get("email");
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Utilisateur introuvable !");
        }

        // Génération d'un token temporaire (valable 15 minutes)
        String token = jwtTokenProvider.generateTokenWithExpiration(email, 15 * 60 * 1000);

        // Stockage du token dans la base de données pour éviter les réutilisations frauduleuses
        User user = userOpt.get();
        user.setResetPasswordToken(token);
        user.setTokenExpiration(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        // Envoi du lien de réinitialisation par email
        String resetLink = "https://mon-site.com/reset-password?token=" + token;
        String emailContent = "Bonjour,\n\nCliquez sur ce lien pour réinitialiser votre mot de passe :\n" 
                              + resetLink + 
                              "\n\n⚠️ Ce lien est valable 15 minutes.";
        mailService.sendEmail(email, "Réinitialisation de mot de passe", emailContent);

        return ResponseEntity.ok(Map.of("message", "Un email de réinitialisation a été envoyé."));
    }

    /**
     * Étape 2 : Réinitialisation du mot de passe avec le token.
     * 
     * - Vérifie si le token est valide et non expiré.
     * - Vérifie si le token correspond bien à celui stocké en base.
     * - Valide la robustesse du nouveau mot de passe.
     * - Met à jour le mot de passe après l'avoir sécurisé.
     * - Supprime le token pour éviter les réutilisations.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        
        // Récupération des données de la requête
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        // Vérifier si le token est valide
        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.badRequest().body("Token invalide ou expiré !");
        }

        // Récupérer l'email depuis le token
        String email = jwtTokenProvider.getUserEmailFromToken(token);
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Utilisateur introuvable !");
        }

        User user = userOpt.get();

        // Vérifier si le token en base est le même que celui fourni
        if (!token.equals(user.getResetPasswordToken()) || LocalDateTime.now().isAfter(user.getTokenExpiration())) {
            return ResponseEntity.badRequest().body("Token invalide ou expiré !");
        }

        // Vérifier la robustesse du mot de passe
        if (newPassword.length() < 8 || 
            !newPassword.matches(".*[A-Z].*") || 
            !newPassword.matches(".*[0-9].*")) {
            return ResponseEntity.badRequest().body("Le mot de passe doit contenir au moins 8 caractères, une majuscule et un chiffre !");
        }

        // Mise à jour du mot de passe après l'avoir encodé
        user.setPassword(passwordEncoder.encode(newPassword));

        // Suppression du token pour éviter une réutilisation
        user.setResetPasswordToken(null);
        user.setTokenExpiration(null);
        userRepository.save(user);

        return ResponseEntity.ok("Mot de passe mis à jour avec succès !");
    }
}
