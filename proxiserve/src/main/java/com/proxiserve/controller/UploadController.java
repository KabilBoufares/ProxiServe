package com.proxiserve.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
@CrossOrigin
public class UploadController {

    private final Cloudinary cloudinary;
    private static final Logger logger = LoggerFactory.getLogger(UploadController.class);

    @PostMapping("/image")
    @PreAuthorize("hasAuthority('ROLE_ARTISAN')") // ou 'ROLE_CLIENT' selon les besoins
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        logger.info("[UPLOAD] Tentative d'upload d'image...");

        if (file.isEmpty()) {
            logger.warn("[UPLOAD] Aucun fichier reçu.");
            return ResponseEntity.badRequest().body("Fichier vide.");
        }

        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            logger.info("[UPLOAD] Image téléchargée avec succès : {}", uploadResult.get("url"));
            return ResponseEntity.ok(uploadResult);
        } catch (IOException e) {
            logger.error("[UPLOAD] Échec de l'upload de l'image", e);
            return ResponseEntity.internalServerError().body("Erreur lors de l'upload.");
        }
    }
}
