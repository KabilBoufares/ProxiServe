package com.proxiserve.controller;

import com.proxiserve.dto.ServiceRequest;
import com.proxiserve.model.Artisan;
import com.proxiserve.model.ServiceEntity;
import com.proxiserve.repository.ArtisanRepository;
import com.proxiserve.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceRepository serviceRepository;
    private final ArtisanRepository artisanRepository;

    @PostMapping
    public ResponseEntity<?> createService(@RequestBody ServiceRequest request, Principal principal) {
        String email = principal.getName();

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
}
