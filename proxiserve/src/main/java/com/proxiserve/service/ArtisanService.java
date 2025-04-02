package com.proxiserve.service;

import org.springframework.data.geo.Point;
import org.springframework.data.geo.Metrics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.proxiserve.model.Artisan;
import com.proxiserve.model.Review;
import com.proxiserve.repository.ArtisanRepository;
import com.proxiserve.repository.ReviewRepository;

import java.util.List;

/**
 * Service pour la gestion des artisans, notamment la recherche géographique.
 */
@Service
public class ArtisanService {

    private static final Logger logger = LoggerFactory.getLogger(ArtisanService.class);
    private final ArtisanRepository artisanRepository;

    /**
     * Constructeur avec injection de dépendances.
     * @param artisanRepository Référentiel des artisans.
     */
    @Autowired
    private ReviewRepository reviewRepository;

    public ArtisanService(ArtisanRepository artisanRepository, ReviewRepository reviewRepository) {
        this.artisanRepository = artisanRepository;
        this.reviewRepository = reviewRepository;
    }
    
    /**
     * Recherche des artisans à proximité d'une localisation donnée.
     * @param latitude  Latitude du point de référence.
     * @param longitude Longitude du point de référence.
     * @param radiusInKm Rayon de recherche en kilomètres.
     * @return Liste des artisans trouvés dans la zone spécifiée.
     * @throws IllegalArgumentException si les paramètres sont invalides.
     */
    public List<Artisan> findNearbyArtisans(double latitude, double longitude, double radiusInKm) {
        // Vérification des valeurs d'entrée
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            logger.error(" Coordonnées invalides : latitude={}, longitude={}", latitude, longitude);
            throw new IllegalArgumentException("Les coordonnées GPS fournies sont invalides.");
        }

        if (radiusInKm <= 0) {
            logger.error(" Rayon de recherche invalide : {}", radiusInKm);
            throw new IllegalArgumentException("Le rayon de recherche doit être un nombre positif.");
        }

        Point location = new Point(longitude, latitude);
        Distance distance = new Distance(radiusInKm, Metrics.KILOMETERS);

        logger.info("🔍 Recherche des artisans proches de [{}, {}] dans un rayon de {} km", latitude, longitude, radiusInKm);
        logger.debug("📡 Paramètres de recherche : Point({}, {}), Distance = {} km", longitude, latitude, radiusInKm);

        List<Artisan> artisans = artisanRepository.findByLocationNear(location, distance);

        if (artisans.isEmpty()) {
            logger.warn(" Aucun artisan trouvé à proximité de [{}, {}] dans un rayon de {} km", latitude, longitude, radiusInKm);
        } else {
            logger.info(" {} artisans trouvés à proximité de [{}, {}]", artisans.size(), latitude, longitude);
        }

        return artisans;
    }

    public double calculateAverageRating(String artisanId) {
        List<Review> reviews = reviewRepository.findByArtisanId(artisanId);
        double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        logger.debug(" Note moyenne calculée pour artisan {} : {} ({} avis)", artisanId, avg, reviews.size());
        return avg;
    }
    
    

}
