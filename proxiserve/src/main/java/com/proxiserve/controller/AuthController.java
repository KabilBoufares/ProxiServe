package com.proxiserve.controller;

import com.proxiserve.dto.SignupRequest;
import com.proxiserve.dto.LoginRequest;
import com.proxiserve.model.Artisan;
import com.proxiserve.model.User;
import com.proxiserve.model.Client;
import com.proxiserve.repository.ArtisanRepository;
import com.proxiserve.repository.ClientRepository;
import com.proxiserve.repository.UserRepository;
import com.proxiserve.security.jwt.JwtTokenProvider;
import com.proxiserve.service.LoginAttemptService;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.BadCredentialsException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ArtisanRepository artisanRepository;
    private final ClientRepository clientRepository;

    @Autowired
    private LoginAttemptService loginAttemptService;



    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody @Valid SignupRequest request) {
        logger.info("Tentative d'inscription avec l'email : {}", request.getEmail());

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.warn("Inscription échouée : Email déjà utilisé - {}", request.getEmail());
            return ResponseEntity.badRequest().body("Erreur : cet email est déjà utilisé !");
        }

        List<String> validRoles = Arrays.asList("ROLE_CLIENT", "ROLE_ARTISAN", "ROLE_ADMIN");
        String role = request.getRole() != null ? request.getRole().toUpperCase() : "ROLE_CLIENT";

        if (!validRoles.contains(role)) {
            return ResponseEntity.badRequest().body("Erreur : rôle invalide !");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(encodedPassword);
        user.setRole(role);
        user.setFullName(request.getFullName() != null ? request.getFullName() : "Inconnu");
        user.setPhoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber() : "N/A");
        userRepository.save(user);

        if ("ROLE_CLIENT".equals(role)) {
            Client client = new Client();
            client.setUserId(user.getId());
            client.setFullName(user.getFullName());
            client.setPhoneNumber(user.getPhoneNumber());
            clientRepository.save(client);
        }

        if ("ROLE_ARTISAN".equals(role)) {
            Artisan artisan = new Artisan();
            artisan.setEmail(user.getEmail());
            artisan.setUserId(user.getId());
            artisan.setPhoneNumber(user.getPhoneNumber());
            artisan.setProfession(request.getProfession() != null ? request.getProfession() : "Non spécifié");
            artisan.setCompanyName(request.getCompanyName() != null ? request.getCompanyName() : "Entreprise inconnue");
            artisan.setServiceCategories(request.getServiceCategories() != null ? request.getServiceCategories() : List.of("Général"));

            Double latitude = request.getLatitude();
            Double longitude = request.getLongitude();
            if (latitude == null || longitude == null) {
                logger.warn("Inscription échouée : coordonnées géographiques manquantes pour l'artisan - {}", request.getEmail());
                return ResponseEntity.badRequest().body("Erreur : latitude et longitude sont requises pour les artisans.");
            }

            artisan.setLocation(new GeoJsonPoint(longitude, latitude));
            artisanRepository.save(artisan);
        }

        return ResponseEntity.ok("Utilisateur enregistré avec succès avec le rôle : " + role);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody @Valid LoginRequest credentials) {
        String email = credentials.getEmail();
        logger.info("Tentative de connexion pour l'email : {}", email);

        if (loginAttemptService.isBlocked(email)) {
            logger.warn("Compte bloqué pour 15 minutes - Email : {}", email);
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body("Trop de tentatives échouées. Compte bloqué pour 15 minutes.");
        }

        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BadCredentialsException("Utilisateur non trouvé"));

            if (!passwordEncoder.matches(credentials.getPassword(), user.getPassword())) {
                throw new BadCredentialsException("Mot de passe incorrect");
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, credentials.getPassword())
            );

            String token = jwtTokenProvider.generateToken(authentication);
            loginAttemptService.loginSucceeded(email);
            logger.info("Connexion réussie - Email : {}", email);
            return ResponseEntity.ok(Map.of("token", token));

        } catch (BadCredentialsException e) {
            loginAttemptService.loginFailed(email);
            logger.warn("Échec de connexion - Identifiants invalides - Email : {}", email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou mot de passe incorrect");
        }
    }

    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        try {
            token = token.replace("Bearer ", "").trim();
            boolean isValid = jwtTokenProvider.validateToken(token);
            logger.info("Vérification du token : Valide = {}", isValid);
            return ResponseEntity.ok(Map.of("valid", isValid));
        } catch (Exception e) {
            logger.warn("Token invalide ou expiré");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide ou expiré");
        }
    }
}
