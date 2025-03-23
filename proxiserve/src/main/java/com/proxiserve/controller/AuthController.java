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

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/auth")
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

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
                          PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider,
                          ArtisanRepository artisanRepository, ClientRepository clientRepository) {  
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.artisanRepository = artisanRepository;
        this.clientRepository = clientRepository;
    }

    /**
     * Inscription d'un nouvel utilisateur.
     * Vérifie si l'email est déjà utilisé et enregistre un nouvel utilisateur avec le rôle spécifié.
     * Si le rôle est "ROLE_CLIENT", un profil Client est créé.
     * Si le rôle est "ROLE_ARTISAN", un profil Artisan est créé.
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody @Valid SignupRequest request) {
        logger.info("Tentative d'inscription avec l'email : {}", request.getEmail());

        // Vérifie si l'email existe déjà
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.warn("Inscription échouée : Email déjà utilisé - {}", request.getEmail());
            return ResponseEntity.badRequest().body("Erreur : cet email est déjà utilisé !");
        }

    // Assigner un rôle par défaut si non fourni
    List<String> validRoles = Arrays.asList("ROLE_CLIENT", "ROLE_ARTISAN", "ROLE_ADMIN");
    String role = request.getRole() != null ? request.getRole().toUpperCase() : "ROLE_CLIENT";

        if (!validRoles.contains(role)) {
            return ResponseEntity.badRequest().body("Erreur : rôle invalide !");
        }


    // Hachage sécurisé du mot de passe
    String encodedPassword = passwordEncoder.encode(request.getPassword());

    // Création de l'utilisateur
    User user = new User();
    user.setEmail(request.getEmail());
    user.setPassword(encodedPassword);
    user.setRole(role);
    userRepository.save(user);

    // Création du profil en fonction du rôle
    if ("ROLE_CLIENT".equals(role)) {
        Client client = new Client();
        client.setUserId(user.getId());
        client.setFullName(request.getFullName() != null ? request.getFullName() : "Inconnu");
        client.setPhoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber() : "N/A");
        clientRepository.save(client);
    }

    if ("ROLE_ARTISAN".equals(role)) {
        Artisan artisan = new Artisan();
        artisan.setUserId(user.getId());
        artisan.setProfession(request.getProfession() != null ? request.getProfession() : "Non spécifié");
        artisan.setCompanyName(request.getCompanyName() != null ? request.getCompanyName() : "Entreprise inconnue");
        artisan.setServiceCategories(request.getServiceCategories() != null ? request.getServiceCategories() : List.of("Général"));
        artisan.setLocation(new GeoJsonPoint(request.getLongitude(), request.getLatitude()));
        artisanRepository.save(artisan);
    }

    return ResponseEntity.ok("Utilisateur enregistré avec succès avec le rôle : " + role);
}


    /**
     * Authentification de l'utilisateur et génération d'un token JWT.
     * Bloque temporairement les utilisateurs après plusieurs échecs de connexion.
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody @Valid LoginRequest credentials) {
        String email = credentials.getEmail();
        logger.info("Tentative de connexion pour l'email : {}", email);

        // Vérifie si le compte est bloqué
        if (loginAttemptService.isBlocked(email)) {
            logger.warn("Compte bloqué pour 15 minutes - Email : {}", email);
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body("Trop de tentatives échouées. Compte bloqué pour 15 minutes.");
        }

        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BadCredentialsException("Utilisateur non trouvé"));

            // Vérification correcte du mot de passe avec BCrypt
            if (!passwordEncoder.matches(credentials.getPassword(), user.getPassword())) {
                throw new BadCredentialsException("Mot de passe incorrect");
            }

            // Authentification réussie
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, credentials.getPassword())
            );

            // Génération du token JWT si l'authentification est réussie 
            String token = jwtTokenProvider.generateToken(authentication);
            //Marquer la connexion comme réussie pour débloquer le compte
            loginAttemptService.loginSucceeded(email);
            logger.info("Connexion réussie - Email : {}", email);
            return ResponseEntity.ok(Map.of("token", token));

        } catch (BadCredentialsException e) {
            loginAttemptService.loginFailed(email);
            logger.warn("Échec de connexion - Identifiants invalides - Email : {}", email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou mot de passe incorrect");
        }
    }


    /**
     * Vérification de la validité d'un token JWT.
     */
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
