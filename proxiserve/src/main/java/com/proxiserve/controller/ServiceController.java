package com.proxiserve.controller;

import com.proxiserve.dto.ServiceRequest;
import com.proxiserve.model.Artisan;
import com.proxiserve.model.ServiceEntity;
import com.proxiserve.model.User;
import com.proxiserve.repository.ArtisanRepository;
import com.proxiserve.repository.ServiceRepository;
import com.proxiserve.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceRepository serviceRepository;
    private final ArtisanRepository artisanRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createService(@RequestBody ServiceRequest request, Principal principal) {
        String email = principal.getName();
        System.out.println("[DEBUG] Principal connecté : " + email);
        //Debug  Récupération de l'artisan
        Artisan artisan = artisanRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Artisan non trouvé"));

        ServiceEntity service = new ServiceEntity();
        service.setTitle(request.getTitle());
        service.setDescription(request.getDescription());
        service.setPrice(request.getPrice());
        service.setArtisanId(artisan.getId());

        serviceRepository.save(service);

        return ResponseEntity.ok("Service créé avec succès");
    }

    // Récupérer tous les services

    @GetMapping
    public ResponseEntity<List<ServiceEntity>> getAllServices() {
        List<ServiceEntity> services = serviceRepository.findAll();
        return ResponseEntity.ok(services);
    }

    // Récupérer un service par son ID

    @GetMapping("/{id}")
    public ResponseEntity<?> getServiceById(@PathVariable String id) {
        Optional<ServiceEntity> serviceOpt = serviceRepository.findById(id);
        
        if (serviceOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Service non trouvé avec l'ID : " + id);
        }

        return ResponseEntity.ok(serviceOpt.get());
}
    // Mettre à jour un service
    @PutMapping("/{id}")
    public ResponseEntity<?> updateService(@PathVariable String id, @RequestBody ServiceEntity serviceUpdate) {
        Optional<ServiceEntity> existingServiceOpt = serviceRepository.findById(id);

        if (existingServiceOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Service non trouvé avec l'ID : " + id);
        }

        ServiceEntity existingService = existingServiceOpt.get();

       // Mettre à jour uniquement les champs non-nuls du service envoyé
        if (serviceUpdate.getTitle() != null) {
            existingService.setTitle(serviceUpdate.getTitle());
        }

        if (serviceUpdate.getDescription() != null)
            existingService.setDescription(serviceUpdate.getDescription());

        if (serviceUpdate.getPrice() != null)
            existingService.setPrice(serviceUpdate.getPrice());

        if (serviceUpdate.getArtisanId() != null)
            existingService.setArtisanId(serviceUpdate.getArtisanId());

        serviceRepository.save(existingService);

        return ResponseEntity.ok(existingService);
    }

    //supprimer un service par son ID
    @DeleteMapping("/{id}")
public ResponseEntity<?> deleteService(
        @PathVariable String id,
        @AuthenticationPrincipal UserDetails userDetails) {

    Optional<ServiceEntity> serviceOpt = serviceRepository.findById(id);

    if (serviceOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body("Service non trouvé avec l'ID : " + id);
    }

    ServiceEntity service = serviceOpt.get();

    String email = userDetails.getUsername();
    Optional<User> userOpt = userRepository.findByEmail(email);

    if (userOpt.isEmpty()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non trouvé");
    }

    User user = userOpt.get();
    String role = user.getRole();

    // ADMIN peut tout supprimer
    if ("ROLE_ADMIN".equals(role)) {
        serviceRepository.deleteById(id);
        return ResponseEntity.ok("Service supprimé par l'admin");
    }

    // ARTISAN doit être propriétaire du service
    if ("ROLE_ARTISAN".equals(role)) {
        Optional<Artisan> artisanOpt = artisanRepository.findByUserId(user.getId());
        if (artisanOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body("Artisan introuvable pour cet utilisateur.");
        }

        Artisan artisan = artisanOpt.get();

        if (!service.getArtisanId().equals(artisan.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body("Vous n'avez pas le droit de supprimer ce service.");
        }

        serviceRepository.deleteById(id);
        return ResponseEntity.ok("Service supprimé par son propriétaire");
    }

    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                         .body("Vous n'avez pas les droits suffisants.");
}


}
