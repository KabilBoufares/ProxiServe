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

    /**
     * Injection des dépendances via le constructeur.
     */
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
     * Vérifie si l'email est déjà utilisé, puis crée un compte avec un rôle défini (CLIENT ou ARTISAN).
     * @param request Informations d'inscription
     * @return Réponse de succès ou d'échec
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody @Valid SignupRequest request) {
        logger.info("🔹 Tentative d'inscription avec l'email : {}", request.getEmail());

        // Vérifie si l'email existe déjà dans la base de données
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            logger.warn("❌ Inscription échouée : Email déjà utilisé - {}", request.getEmail());
            return ResponseEntity.badRequest().body("Erreur : cet email est déjà utilisé !");
        }

        // Vérification du rôle et attribution par défaut à "ROLE_CLIENT" si null
        String role = request.getRole() != null ? request.getRole().toUpperCase() : "ROLE_CLIENT";

        if (!role.equals("ROLE_CLIENT") && !role.equals("ROLE_ARTISAN")) {
            logger.warn("⚠️ Inscription échouée : Rôle invalide fourni - {}", role);
            return ResponseEntity.badRequest().body("Erreur : rôle invalide !");
        }

        // Création de l'utilisateur
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Hachage sécurisé du mot de passe
        user.setRole(role);
        userRepository.save(user);

        // Création d'un profil Client
        if ("ROLE_CLIENT".equals(role)) {
            try {
                logger.info("✅ Enregistrement du client...");

                Client client = new Client();
                client.setUserId(user.getId());
                client.setFullName(request.getFullName());
                client.setPhoneNumber(request.getPhoneNumber());

                clientRepository.save(client);
                logger.info("✅ Client enregistré avec succès : {}", client.getFullName());
            } catch (Exception e) {
                logger.error("❌ Erreur lors de l'enregistrement du client : {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'enregistrement du client.");
            }
        }

        // Création d'un profil Artisan
        if ("ROLE_ARTISAN".equals(role)) {
            try {
                logger.info("✅ Enregistrement de l'artisan...");

                Artisan artisan = new Artisan();
                artisan.setUserId(user.getId());
                artisan.setProfession(request.getProfession());
                artisan.setCompanyName(request.getCompanyName());
                artisan.setServiceCategories(request.getServiceCategories());
                artisan.setLocation(new GeoJsonPoint(request.getLongitude(), request.getLatitude()));

                artisanRepository.save(artisan);
                logger.info("✅ Artisan enregistré avec succès : {}", artisan.getCompanyName());
            } catch (Exception e) {
                logger.error("❌ Erreur lors de l'enregistrement de l'artisan : {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'enregistrement de l'artisan.");
            }
        }

        return ResponseEntity.ok("Utilisateur enregistré avec succès avec le rôle : " + role);
    }

    /**
     * Authentification de l'utilisateur et génération d'un token JWT.
     * Bloque temporairement les utilisateurs après plusieurs échecs.
     * @param credentials Identifiants de connexion
     * @return Token JWT si connexion réussie
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody @Valid LoginRequest credentials) {
        String email = credentials.getEmail();
        logger.info("🔹 Tentative de connexion pour l'email : {}", email);

        // Vérifie si le compte est bloqué après plusieurs tentatives échouées
        if (loginAttemptService.isBlocked(email)) {
            logger.warn("🚫 Compte bloqué pour 15 minutes - Email : {}", email);
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body("Trop de tentatives échouées. Compte bloqué pour 15 minutes.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, credentials.getPassword())
            );
            String token = jwtTokenProvider.generateToken(authentication);
            loginAttemptService.loginSucceeded(email);
            logger.info("✅ Connexion réussie - Email : {}", email);
            return ResponseEntity.ok(Map.of("token", token));
        } catch (BadCredentialsException e) {
            loginAttemptService.loginFailed(email);
            logger.warn("❌ Échec de connexion - Identifiants invalides - Email : {}", email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou mot de passe incorrect");
        }
    }

    /**
     * Vérification de la validité d'un token JWT.
     * @param token Token JWT à valider
     * @return Réponse indiquant si le token est valide
     */
    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        try {
            token = token.replace("Bearer ", "").trim();
            boolean isValid = jwtTokenProvider.validateToken(token);
            logger.info("🔹 Vérification du token : Valide = {}", isValid);
            return ResponseEntity.ok(Map.of("valid", isValid));
        } catch (Exception e) {
            logger.warn("❌ Token invalide ou expiré");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide ou expiré");
        }
    }
}
