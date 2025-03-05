package com.proxiserve.service;

import org.springframework.data.geo.Point;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Distance;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.proxiserve.model.Artisan;
import com.proxiserve.repository.ArtisanRepository;

import java.util.List;

@Service
public class ArtisanService {

    private static final Logger logger = LoggerFactory.getLogger(ArtisanService.class);
    private final ArtisanRepository artisanRepository;

    public ArtisanService(ArtisanRepository artisanRepository) {
        this.artisanRepository = artisanRepository;
    }

    public List<Artisan> findNearbyArtisans(double latitude, double longitude, double radiusInKm) {
        Point location = new Point(longitude, latitude);
        Distance distance = new Distance(radiusInKm, Metrics.KILOMETERS);

        logger.info("🔍 Recherche des artisans proches de [{}, {}] dans un rayon de {} km", latitude, longitude, radiusInKm);

        List<Artisan> artisans = artisanRepository.findByLocationNear(location, distance);

        if (artisans.isEmpty()) {
            logger.warn("⚠️ Aucun artisan trouvé à proximité de [{}, {}]", latitude, longitude);
        }

        return artisans;
    }
}
