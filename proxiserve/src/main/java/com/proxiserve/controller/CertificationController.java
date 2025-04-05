package com.proxiserve.controller;

import com.proxiserve.model.Certification;
import com.proxiserve.repository.CertificationRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/certifications")
@CrossOrigin
public class CertificationController {

    private final CertificationRepository certificationRepository;

    public CertificationController(CertificationRepository certificationRepository) {
        this.certificationRepository = certificationRepository;
    }

    @PostMapping
    public Certification addCertification(@RequestBody Certification certif) {
        return certificationRepository.save(certif);
    }

    @GetMapping("/artisan/{artisanId}")
    public List<Certification> getByArtisan(@PathVariable String artisanId) {
        return certificationRepository.findByArtisanId(artisanId);
    }

    @DeleteMapping("/{id}")
    public void deleteCertification(@PathVariable String id) {
        certificationRepository.deleteById(id);
    }
}
