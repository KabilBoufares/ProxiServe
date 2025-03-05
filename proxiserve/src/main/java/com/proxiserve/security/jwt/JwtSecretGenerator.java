package com.proxiserve.security.jwt;

import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import javax.crypto.SecretKey;

/*
 * L'erreur WeakKeyException du clé secrète JWT n'est pas  sécurisée pour être utilisée avec l'algorithme HS512.
 * ajouter temporairement à ton code et l'exécuter une seule fois pour générer une nouvelle clé secrète.
 * éxecuter 
 */

public class JwtSecretGenerator {
    public static void main(String[] args) {
        SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS512); // Générer une clé assez longue
        String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
        System.out.println("Generated Secret Key: " + base64Key);
    }
}
