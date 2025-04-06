package com.proxiserve.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.proxiserve.dto.ArtisanProfileView;
import com.proxiserve.model.Artisan;
import com.proxiserve.model.Certification;
import com.proxiserve.repository.ArtisanRepository;
import com.proxiserve.repository.CertificationRepository;
import com.proxiserve.service.ArtisanService;
import com.proxiserve.service.ImageUploadService;
import com.proxiserve.repository.UserRepository; // Add UserRepository import
import com.proxiserve.model.User; // Import the User class

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

/**
 * Contrôleur REST pour gérer les artisans.
 * Fournit des endpoints sécurisés pour récupérer les artisans à proximité.
 */
@RestController
@RequestMapping("/api/artisans")
@RequiredArgsConstructor

public class ArtisanController {

    private static final Logger logger = LoggerFactory.getLogger(ArtisanController.class);
    private final ArtisanService artisanService;
     private final ArtisanRepository artisanRepository;
    private final ImageUploadService imageUploadService;
private final UserRepository userRepository; // Inject UserRepository
private final CertificationRepository certificationRepository; // Inject CertificationRepository


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



    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('ROLE_ARTISAN')")
    public ResponseEntity<Artisan> getAuthenticatedArtisanProfile(Principal principal) {
        String email = principal.getName(); // email extrait du token JWT
        Optional<Artisan> artisanOpt = artisanRepository.findByEmail(email);

        if (artisanOpt.isEmpty()) {
            return ResponseEntity.status(404).body(null);
        }

        return ResponseEntity.ok(artisanOpt.get());
    }


    @GetMapping("/{id}/profile")
public ResponseEntity<ArtisanProfileView> getArtisanProfile(@PathVariable String id) {
    Optional<Artisan> artisanOpt = artisanRepository.findById(id);
    if (artisanOpt.isEmpty()) {
        return ResponseEntity.notFound().build();
    }

    Artisan artisan = artisanOpt.get();
    List<Certification> certifications = certificationRepository.findByArtisanId(id);

    // 🔁 Récupérer le fullName depuis User
    Optional<User> userOpt = userRepository.findById(artisan.getUserId());
    String fullName = userOpt.map(User::getFullName).orElse("Artisan inconnu");

    ArtisanProfileView profile = new ArtisanProfileView();
    profile.setId(artisan.getId());
    profile.setEmail(artisan.getEmail());
    profile.setPhoneNumber(artisan.getPhoneNumber());
    profile.setFullName(fullName); // ✅ injecté depuis User
    profile.setProfession(artisan.getProfession());
    profile.setCompanyName(artisan.getCompanyName());
    profile.setProfilePictureUrl(artisan.getProfilePictureUrl());
    profile.setBiography(artisan.getBiography());
    profile.setSkills(artisan.getSkills());
    profile.setServiceCategories(artisan.getServiceCategories());
    profile.setWorkingHoursWeekdays(artisan.getWorkingHoursWeekdays());
    profile.setWorkingHoursSaturday(artisan.getWorkingHoursSaturday());
    profile.setWorkingHoursSunday(artisan.getWorkingHoursSunday());
    profile.setWorkPhotoUrls(artisan.getWorkPhotoUrls());
    profile.setCertifications(certifications);

    return ResponseEntity.ok(profile);
}


    @PutMapping("/{id}/profile")
    @PreAuthorize("hasAuthority('ROLE_ARTISAN')") // ou check plus fin avec userId
    public ResponseEntity<?> updateProfile(@PathVariable String id, @RequestBody Artisan updatedData) {
        Optional<Artisan> artisanOpt = artisanRepository.findById(id);
        if (artisanOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Artisan artisan = artisanOpt.get();

        
        artisan.setPhoneNumber(updatedData.getPhoneNumber());
        artisan.setProfilePictureUrl(updatedData.getProfilePictureUrl());
        artisan.setBiography(updatedData.getBiography());
        artisan.setSkills(updatedData.getSkills());
        artisan.setProfession(updatedData.getProfession());
        artisan.setCompanyName(updatedData.getCompanyName());
        artisan.setServiceCategories(updatedData.getServiceCategories());
        artisan.setWorkingHoursWeekdays(updatedData.getWorkingHoursWeekdays());
        artisan.setWorkingHoursSaturday(updatedData.getWorkingHoursSaturday());
        artisan.setWorkingHoursSunday(updatedData.getWorkingHoursSunday());
        artisan.setWorkPhotoUrls(updatedData.getWorkPhotoUrls());

        if (updatedData.getLocation() != null) {
            artisan.setLocation(updatedData.getLocation());
        }

        artisanRepository.save(artisan);
        return ResponseEntity.ok("Profil mis à jour avec succès.");
    }


    @DeleteMapping("/{id}/photos")
    @PreAuthorize("hasAuthority('ROLE_ARTISAN')")
    public ResponseEntity<?> deleteWorkPhoto(@PathVariable String id, @RequestParam String photoUrl) {
        Optional<Artisan> artisanOpt = artisanRepository.findById(id);
        if (artisanOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Artisan artisan = artisanOpt.get();
        List<String> photos = artisan.getWorkPhotoUrls();
        if (photos.remove(photoUrl)) {
            artisan.setWorkPhotoUrls(photos);
            artisanRepository.save(artisan);
            return ResponseEntity.ok("Photo supprimée.");
        } else {
            return ResponseEntity.badRequest().body("Photo introuvable.");
        }
    }


    @DeleteMapping("/{id}/skills")
    @PreAuthorize("hasAuthority('ROLE_ARTISAN')")
    public ResponseEntity<?> deleteSkill(@PathVariable String id, @RequestParam String skill) {
        Optional<Artisan> artisanOpt = artisanRepository.findById(id);
        if (artisanOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Artisan artisan = artisanOpt.get();
        List<String> skills = artisan.getSkills();
        if (skills.removeIf(s -> s.equalsIgnoreCase(skill))) {
            artisan.setSkills(skills);
            artisanRepository.save(artisan);
            return ResponseEntity.ok("Compétence supprimée.");
        } else {
            return ResponseEntity.badRequest().body("Compétence non trouvée.");
        }
    }

    @PostMapping("/profile-picture")
    @PreAuthorize("hasAuthority('ROLE_ARTISAN')")
    public ResponseEntity<?> uploadProfilePicture(@RequestParam("file") MultipartFile file, Principal principal) {
        String email = principal.getName();
        logger.info("[UPLOAD] - Artisan tente d'uploader une photo de profil : {}", email);

        Optional<Artisan> artisanOpt = artisanRepository.findByEmail(email);
        if (artisanOpt.isEmpty()) {
            logger.warn("[ERREUR] - Artisan non trouvé pour l'email : {}", email);
            return ResponseEntity.badRequest().body("Artisan non trouvé.");
        }

        Artisan artisan = artisanOpt.get();
        try {
            String imageUrl = imageUploadService.uploadImage(file);
            artisan.setProfilePictureUrl(imageUrl);
            artisanRepository.save(artisan);
            logger.info("[SUCCÈS] - Photo de profil mise à jour pour l'artisan {}", artisan.getId());
            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            logger.error("[ERREUR] - Upload échoué pour l'artisan {} : {}", artisan.getId(), e.getMessage());
            return ResponseEntity.internalServerError().body("Erreur lors de l’upload.");
        }
    }

    @PostMapping("/work-photo")
    @PreAuthorize("hasAuthority('ROLE_ARTISAN')")
    public ResponseEntity<?> uploadWorkPhoto(@RequestParam("file") MultipartFile file, Principal principal) {
        String email = principal.getName();
        logger.info("[UPLOAD] - Artisan tente d'uploader une photo de travail : {}", email);

        Optional<Artisan> artisanOpt = artisanRepository.findByEmail(email);
        if (artisanOpt.isEmpty()) {
            logger.warn("[ERREUR] - Artisan non trouvé pour l'email : {}", email);
            return ResponseEntity.badRequest().body("Artisan non trouvé.");
        }

        Artisan artisan = artisanOpt.get();
        try {
            String imageUrl = imageUploadService.uploadImage(file);
            artisan.getWorkPhotoUrls().add(imageUrl);
            artisanRepository.save(artisan);
            logger.info("[SUCCÈS] - Nouvelle photo de travail ajoutée pour l'artisan {}", artisan.getId());
            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            logger.error("[ERREUR] - Upload échoué pour l'artisan {} : {}", artisan.getId(), e.getMessage());
            return ResponseEntity.internalServerError().body("Erreur lors de l’upload.");
        }
    }


}
