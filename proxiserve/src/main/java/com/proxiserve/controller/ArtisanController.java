package com.proxiserve.controller;

import org.springframework.web.bind.annotation.*;

import com.proxiserve.model.Artisan;
import com.proxiserve.service.ArtisanService;


import java.util.List;

/*@RestController
@RequestMapping("/api/artisans")
public class ArtisanController {

    @Autowired
    private ArtisanService artisanService;

    @GetMapping("/nearby")
    public List<Artisan> getNearbyArtisans(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam double radius) {
        return artisanService.findNearbyArtisans(latitude, longitude, radius);
    }
}*/






import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;




@RestController
@RequestMapping("/api/artisans")
public class ArtisanController {

    private final ArtisanService artisanService;

    // Constructor Injection (Meilleure pratique)
    public ArtisanController(ArtisanService artisanService) {
        this.artisanService = artisanService;
    }

    /**
     * API sécurisée : Seuls les utilisateurs authentifiés peuvent accéder à la liste des artisans proches.
     *  Vérification des paramètres pour éviter les requêtes invalides.
     */
    @GetMapping("/nearby")
    @PreAuthorize("hasRole('ROLE_CLIENT')")  // Sécurisation
    public ResponseEntity<List<Artisan>> getNearbyArtisans(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam double radius) {
        
        //  Vérification des paramètres avant exécution
        if (radius <= 0) {
            return ResponseEntity.badRequest().body(null);
        }

        List<Artisan> artisans = artisanService.findNearbyArtisans(latitude, longitude, radius);
        
        if (artisans.isEmpty()) {
            return ResponseEntity.noContent().build(); // Renvoie 204 No Content si aucun artisan trouvé
        }

        return ResponseEntity.ok(artisans);
    }
}

