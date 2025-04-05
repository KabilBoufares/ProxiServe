package com.proxiserve.controller;

import com.proxiserve.model.Artisan;
import com.proxiserve.model.Certification;
import com.proxiserve.repository.ArtisanRepository;
import com.proxiserve.repository.CertificationRepository;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

/**
 * Contrôleur REST pour la gestion des certifications liées aux artisans.
 */
@RestController
@RequestMapping("/api/certifications")
@CrossOrigin
@RequiredArgsConstructor
public class CertificationController {

    private static final Logger logger = LoggerFactory.getLogger(CertificationController.class);

    private final CertificationRepository certificationRepository;
    private final ArtisanRepository artisanRepository;

    /**
     *  Ajouter une certification pour l'artisan connecté.
     * L'ID de l'artisan est récupéré automatiquement depuis le token (email).
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ARTISAN')")
    public ResponseEntity<?> addCertification(@RequestBody Certification certif, Principal principal) {
        String email = principal.getName(); // Email extrait depuis le JWT

        logger.info(" [POST] Demande d'ajout de certification pour l'artisan avec email : {}", email);

        // 🔍 Recherche de l'artisan par email
        Optional<Artisan> artisanOpt = artisanRepository.findByEmail(email);
        if (artisanOpt.isEmpty()) {
            logger.warn(" Artisan introuvable avec l'email : {}", email);
            return ResponseEntity.badRequest().body("Artisan introuvable.");
        }

        Artisan artisan = artisanOpt.get();

        //  Association de l'artisan à la certification
        certif.setArtisanId(artisan.getId());

        //  Enregistrement
        Certification saved = certificationRepository.save(certif);
        logger.info(" Certification ajoutée avec succès pour l'artisan : {}", artisan.getId());

        return ResponseEntity.ok(saved);
    }

    /**
     *  Récupérer toutes les certifications d’un artisan.
     * Accessible à tout utilisateur pour visualiser un profil complet.
     */
    @GetMapping("/artisan/{artisanId}")
    public ResponseEntity<List<Certification>> getByArtisan(@PathVariable String artisanId) {
        logger.info("📤 [GET] Récupération des certifications pour artisanId = {}", artisanId);

        List<Certification> certifs = certificationRepository.findByArtisanId(artisanId);

        return ResponseEntity.ok(certifs);
    }

    /**
     *  Supprimer une certification (seulement pour les artisans).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ARTISAN')")
    public ResponseEntity<?> deleteCertification(@PathVariable String id) {
        logger.info("🗑️ [DELETE] Suppression de la certification avec ID = {}", id);

        if (!certificationRepository.existsById(id)) {
            logger.warn(" Aucune certification trouvée avec ID = {}", id);
            return ResponseEntity.notFound().build();
        }

        certificationRepository.deleteById(id);
        logger.info(" Certification supprimée avec succès (ID = {})", id);

        return ResponseEntity.ok("Certification supprimée avec succès.");
    }
}
