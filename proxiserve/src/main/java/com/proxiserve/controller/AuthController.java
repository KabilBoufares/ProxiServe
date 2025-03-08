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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

import org.springframework.security.authentication.BadCredentialsException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ArtisanRepository artisanRepository;
    private final ClientRepository clientRepository;

    // Injection des dépendances via le constructeur
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
     * Inscription d'un nouvel utilisateur avec validation.
     * Vérifie si l'email est déjà utilisé avant d'enregistrer un nouvel utilisateur.
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody @Valid SignupRequest request) {
        // Vérifie si l'email existe déjà dans la base de données
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Erreur : cet email est déjà utilisé !");
        }
    
        // Vérifie si un rôle est fourni, sinon attribue un rôle par défaut ("CLIENT")
        String role = request.getRole() != null ? request.getRole().toUpperCase() : "CLIENT";
    
        // Création de l'utilisateur avec email, mot de passe et rôle
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Hachage sécurisé du mot de passe
        user.setRole(role);

        userRepository.save(user);

        /* 
        // Si l'utilisateur est un client, créer et enregistrer un objet Client
        if ("CLIENT".equalsIgnoreCase(role)) {
            Client client = new Client();
            client.setUserId(user.getId()); // Associer le client à l'utilisateur
            client.setFullName(request.getFullName()); // Récupérer depuis SignupRequest
            client.setPhoneNumber(request.getPhoneNumber());
            clientRepository.save(client); // Sauvegarder dans MongoDB
            System.out.println("Client enregistré avec succès: " + client.getFullName()); //tester si le client est bien enregistré
        }
            */

            if ("CLIENT".equalsIgnoreCase(role)) {
                System.out.println("Début de l'enregistrement du client...");
                
                Client client = new Client();
                client.setUserId(user.getId()); // Associer le client à l'utilisateur
                client.setFullName(request.getFullName()); // Vérifie que SignupRequest a bien `fullName`
                client.setPhoneNumber(request.getPhoneNumber());
        
                try {
                    clientRepository.save(client);// Enregistre le client  dans MongoDB
                    System.out.println(" Client enregistré avec succès: " + client.getFullName());
                } catch (Exception e) {
                    System.err.println(" Erreur lors de l'enregistrement du client: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        



        // Si l'utilisateur est un artisan, créer et enregistrer un objet Artisan
        if ("ARTISAN".equalsIgnoreCase(role)) {
            Artisan artisan = new Artisan();
            artisan.setUserId(user.getId()); // Associe l'artisan à l'utilisateur
            artisan.setProfession(request.getProfession()); // Profession renseignée lors de l'inscription
            artisan.setCompanyName(request.getCompanyName());
            artisan.setServiceCategories(request.getServiceCategories());
            artisan.setLocation(new GeoJsonPoint(request.getLongitude(), request.getLatitude())); // Localisation géographique
            
            artisanRepository.save(artisan); // Enregistre l'artisan dans MongoDB
        }
    
        return ResponseEntity.ok("Utilisateur enregistré avec succès avec le rôle : " + role);
    }
    
    /**
     * Authentification de l'utilisateur et génération d'un token JWT.
     * Gère les erreurs d'identification en renvoyant un code 401 (Unauthorized).
     */
    @Autowired
    private LoginAttemptService loginAttemptService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody @Valid LoginRequest credentials) {
        String email = credentials.getEmail();

        // Vérifie si l'utilisateur est temporairement bloqué après plusieurs tentatives de connexion échouées
        if (loginAttemptService.isBlocked(email)) {
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body("Trop de tentatives échouées. Compte bloqué pour 15 minutes.");
        }

        try {
            // Authentification avec Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, credentials.getPassword())
            );

            // Génération d'un token JWT
            String token = jwtTokenProvider.generateToken(authentication);

            // Réinitialisation des tentatives de connexion après une connexion réussie
            loginAttemptService.loginSucceeded(email);

            return ResponseEntity.ok(Map.of("token", token));

        } catch (BadCredentialsException e) {
            // Incrémente le compteur de tentatives échouées
            loginAttemptService.loginFailed(email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou mot de passe incorrect");
        }
    }

    /**
     * Génération d'un token de réinitialisation du mot de passe.
     * Si l'email est valide, un token temporaire est généré.
     */
    @PostMapping("/reset-password-request")
    public ResponseEntity<?> requestResetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Optional<User> userOpt = userRepository.findByEmail(email);
    
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Email non trouvé !");
        }
    
        // Génère un token temporaire de réinitialisation de mot de passe (validité de 10 minutes)
        String resetToken = jwtTokenProvider.generateTokenWithExpiration(email, 10 * 60 * 1000);
    
        // TODO: Envoyer ce token par email au user (à implémenter plus tard)
        // Pour l'instant, on affiche le token dans la console pour les tests
        System.out.println("Token de réinitialisation pour " + email + ": " + resetToken);
        return ResponseEntity.ok(Map.of("resetToken", resetToken));
    }

    /**
     * Vérification de la validité d'un token JWT.
     * Retourne "valid: true" si le token est valide, sinon "valid: false".
     */
    @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        try {
            token = token.replace("Bearer ", "").trim(); // Supprime "Bearer " du token
            boolean isValid = jwtTokenProvider.validateToken(token);
            return ResponseEntity.ok(Map.of("valid", isValid));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
        }
    }
    
}
