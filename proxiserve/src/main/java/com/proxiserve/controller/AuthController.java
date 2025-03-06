package com.proxiserve.controller;

import com.proxiserve.dto.SignupRequest;
import com.proxiserve.dto.LoginRequest;

import com.proxiserve.model.User;
import com.proxiserve.repository.UserRepository;
import com.proxiserve.security.jwt.JwtTokenProvider;
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

    //  Constructor Injection (Bonne pratique)
    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, 
                          PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     *  Enregistrement d'un nouvel utilisateur avec validation.
     *  Vérifie si l'email est déjà utilisé avant d'enregistrer.
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody @Valid SignupRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: Email is already in use!");
        }
    
        //  Vérifier que le rôle est fourni, sinon assigner un rôle par défaut
        String role = request.getRole() != null ? request.getRole().toUpperCase() : "CLIENT";
    
        //  Création de l'utilisateur avec email, mot de passe et rôle
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Hashage sécurisé du mot de passe
        user.setRole(role);  // 🔹 Ajout du rôle utilisateur
    
        userRepository.save(user);
    
        return ResponseEntity.ok("User registered successfully with role: " + role);
    }
    

    /**
     *  Authentifie l'utilisateur et génère un JWT.
     *  Gère les erreurs d'identification proprement (401 Unauthorized).
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody @Valid LoginRequest credentials) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(credentials.getEmail(), credentials.getPassword())
            );

            String token = jwtTokenProvider.generateToken(authentication);
            return ResponseEntity.ok(Map.of("token", token));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }
    }


    @PostMapping("/reset-password-request")
    public ResponseEntity<?> requestResetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        Optional<User> userOpt = userRepository.findByEmail(email);
    
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Email non trouvé !");
        }
    
        // Générer un token temporaire pour reset password (10 minutes de validité)
        String resetToken = jwtTokenProvider.generateTokenWithExpiration(email, 10 * 60 * 1000);
    
        // TODO: Envoyer ce token par email au user (à implémenter)
        return ResponseEntity.ok(Map.of("resetToken", resetToken));
    }
    
    //endpoint pour vérifier un token
   @GetMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", ""); // Supprime "Bearer " du token
        boolean isValid = jwtTokenProvider.validateToken(token);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }

    


   
}

