package com.proxiserve.controller;

import org.springframework.web.bind.annotation.*;
import com.proxiserve.model.Artisan;
import com.proxiserve.service.ArtisanService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * Contrôleur REST pour gérer les artisans.
 * Fournit des endpoints sécurisés pour récupérer les artisans à proximité.
 */
@RestController
@RequestMapping("/api/artisans")

public class ArtisanController {

    private static final Logger logger = LoggerFactory.getLogger(ArtisanController.class);
    private final ArtisanService artisanService;

    /**
     * Injection de dépendance via le constructeur (bonne pratique).
     * @param artisanService Service permettant de récupérer les artisans
     */
    public ArtisanController(ArtisanService artisanService) {
        this.artisanService = artisanService;
    }

    /**
     * Endpoint sécurisé permettant aux clients de récupérer la liste des artisans proches.
     * Seuls les utilisateurs avec le rôle "ROLE_CLIENT" peuvent y accéder.
     *
     * @param latitude  Latitude du client
     * @param longitude Longitude du client
     * @param radius    Rayon de recherche en kilomètres
     * @return Liste des artisans trouvés dans le rayon spécifié
     */
    @GetMapping("/nearby")
    @PreAuthorize("hasAuthority('ROLE_CLIENT')")
    public ResponseEntity<List<Artisan>> getNearbyArtisans(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam double radius) {

        logger.info(" [INFO] - Requête reçue : Recherche d'artisans proches (lat: {}, long: {}, rayon: {} km)", latitude, longitude, radius);

        // Vérification des paramètres
        if (radius <= 0) {
            logger.warn(" [AVERTISSEMENT] - Rayon de recherche invalide : {}", radius);
            return ResponseEntity.badRequest().body(null);
        }
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            logger.warn(" [AVERTISSEMENT] - Coordonnées invalides : lat={}, long={}", latitude, longitude);
            return ResponseEntity.badRequest().body(null);
        }

        // Recherche des artisans à proximité
        List<Artisan> artisans = artisanService.findNearbyArtisans(latitude, longitude, radius);

        if (artisans.isEmpty()) {
            logger.info("ℹ [INFO] - Aucun artisan trouvé dans le rayon de {} km autour de (lat={}, long={})", radius, latitude, longitude);
            return ResponseEntity.noContent().build();
        }

        // Calcul de la note moyenne pour chaque artisan
        artisans.forEach(artisan -> {
                        double rating = artisanService.calculateAverageRating(artisan.getId());
                                artisan.setAverageRating(rating);
                        });


        logger.info(" [INFO] - {} artisans trouvés dans le rayon de {} km autour de (lat={}, long={})", artisans.size(), radius, latitude, longitude);
        return ResponseEntity.ok(artisans);
    }
}
