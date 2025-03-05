package com.proxiserve.controller;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.proxiserve.model.User;
import com.proxiserve.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import java.util.Optional;

@RestController
@RequestMapping("/api/test") // Ajout d'un préfixe propre pour éviter toute confusion avec HealthCheck
public class TestDataController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ✅ Injection via constructeur (Bonne pratique)
    public TestDataController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/db")
    public ResponseEntity<String> testDB() {
        String testEmail = "test1@hebil123456.com";

        // ✅ Vérification si l'utilisateur existe déjà
        Optional<User> existingUser = userRepository.findByEmail(testEmail);
        if (existingUser.isPresent()) {
            return ResponseEntity.badRequest().body("User already exists in MongoDB!");
        }

        // ✅ Création de l'utilisateur avec mot de passe hashé
        User user = new User();
        user.setEmail(testEmail);
        user.setPassword(passwordEncoder.encode("123456"));  // ✅ Mot de passe sécurisé
        user.setRole("CLIENT");

        userRepository.save(user);
        return ResponseEntity.ok("Data saved to MongoDB securely!");
    }
}
