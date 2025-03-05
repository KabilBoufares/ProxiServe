package com.proxiserve.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.proxiserve.model.Artisan;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import java.util.List;

@Repository
public interface ArtisanRepository extends MongoRepository<Artisan, String> {

    // Recherche des artisans à proximité (Index géospatial requis)
    List<Artisan> findByLocationNear(Point location, Distance distance);

    // Recherche des artisans par profession
    List<Artisan> findByProfessionIgnoreCase(String profession);

    // Recherche des artisans par nom d'entreprise (recherche partielle et insensible à la casse)
    List<Artisan> findByCompanyNameContainingIgnoreCase(String companyName);
}
