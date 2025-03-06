package com.proxiserve.controller;


import com.proxiserve.model.User;
import com.proxiserve.repository.UserRepository;
import com.proxiserve.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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

    // Étape 1 : Demande de réinitialisation de mot de passe
    @PostMapping("/api/auth/request-reset-password")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> request) {

        String email = request.get("email");
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            String token = jwtTokenProvider.generateTokenWithExpiration(email, 15 * 60 * 1000); // 15 min
            return ResponseEntity.ok(Map.of("message", "Password reset token generated", "token", token));
        }
        return ResponseEntity.badRequest().body("User not found");
    }

    // Étape 2 : Réinitialisation du mot de passe avec le token
    

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

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
        user.setPassword(passwordEncoder.encode(newPassword)); // Hash du nouveau mot de passe
        userRepository.save(user);

        return ResponseEntity.ok("Mot de passe mis à jour avec succès !");
    }

}
