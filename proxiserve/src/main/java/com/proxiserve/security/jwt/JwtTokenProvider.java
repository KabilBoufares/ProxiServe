package com.proxiserve.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import javax.crypto.SecretKey;
import java.util.Base64;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    private final SecretKey signingKey;
    private final long jwtExpirationMs;

    public JwtTokenProvider(@Value("${jwt.secret}") String jwtSecret,
                            @Value("${jwt.expiration}") long jwtExpirationMs) {
        //byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);  // Décoder la clé en Base64
        //this.signingKey = Keys.hmacShaKeyFor(keyBytes);  // Clé sécurisée
        this.signingKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret));
        this.jwtExpirationMs = jwtExpirationMs;
    }

    public String generateToken(Authentication authentication) {
        String email = authentication.getName();

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(signingKey, SignatureAlgorithm.HS512)
                .compact();
    }

    public String getUserEmailFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException ex) {
            logger.error("🔴 Erreur de signature JWT invalide: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("🔴 JWT mal formé: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("🔴 JWT expiré: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("🔴 JWT non supporté: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("🔴 JWT claims sont vides: {}", ex.getMessage());
        }
        return false;
    }

    public String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
