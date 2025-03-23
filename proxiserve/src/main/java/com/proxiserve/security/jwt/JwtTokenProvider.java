package com.proxiserve.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import javax.crypto.SecretKey;
import java.util.Base64;

/**
 * Classe responsable de la gestion des tokens JWT.
 * Elle génère, valide et extrait les informations des tokens JWT.
 */
@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secretKey;
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    private final SecretKey signingKey;  // Clé secrète pour signer les tokens
    private final long jwtExpirationMs;  // Durée de validité du token en millisecondes
    

    /**
     * Constructeur qui initialise la clé de signature et la durée d'expiration du JWT.
     * 
     * @param jwtSecret Clé secrète encodée en Base64 (définie dans application.properties)
     * @param jwtExpirationMs Durée de vie du token en millisecondes
     */
    public JwtTokenProvider(@Value("${jwt.secret}") String jwtSecret,
                            @Value("${jwt.expiration}") long jwtExpirationMs) {
        // Décodage de la clé Base64 pour garantir une sécurité optimale
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));

        this.jwtExpirationMs = jwtExpirationMs;
    }

    /**
     * Génère un token JWT pour un utilisateur authentifié.
     *
     * @param authentication Objet contenant les informations de l'utilisateur
     * @return Token JWT signé
     */
    public String generateToken(Authentication authentication) {
        String email = authentication.getName();
    
        // Extraction du rôle et ajout du préfixe "ROLE_"
        String role = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority) // Extrait l'autorité
            .findFirst()
            .orElse("ROLE_CLIENT"); // Définit un rôle par défaut
    
        logger.info("Token généré : Email={} | Role={}", email, role); // Log du token généré
    
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role) // Ajoute "ROLE_" si absent
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }
    

    /**
     * Extrait l'email de l'utilisateur à partir du token JWT.
     *
     * @param token JWT valide
     * @return Email de l'utilisateur
     */
    public String getUserEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Valide un token JWT pour s'assurer qu'il est bien formé et non expiré.
     *
     * @param token JWT à valider
     * @return true si le token est valide, false sinon
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token);
    
            System.out.println(" Token valide : " + token);
            return true;

        } catch (ExpiredJwtException e) {
            System.out.println(" Token expiré !");
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println(" Token invalide !");
        }
        return false;
    }

    /**
     * Extrait le token JWT depuis une requête HTTP.
     * 
     * @param request Requête HTTP contenant le header "Authorization"
     * @return Token JWT si présent, sinon null
     */
    public String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Extraction du token après "Bearer "
        }
        return null;
    }

    /**
     * Génère un token avec une expiration personnalisée (utilisé pour la réinitialisation de mot de passe).
     * 
     * @param email Email de l'utilisateur
     * @param expirationMillis Durée de validité du token en millisecondes
     * @return Token JWT temporaire
     */
    public String generateTokenWithExpiration(String email, long expirationMillis) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }
}
