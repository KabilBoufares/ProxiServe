package com.proxiserve.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.proxiserve.model.Artisan;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtisanRepository extends MongoRepository<Artisan, String> {

    /**
     * Recherche d'un artisan par son identifiant utilisateur.
     * 
     * @param userId Identifiant de l'utilisateur.
     * @return Artisan correspondant à l'identifiant utilisateur.
     */

    Optional<Artisan> findByUserId(String userId);

    /**
     * Recherche d'un artisan par son identifiant.
     * 
     * @param id Identifiant de l'artisan.
     * @return Artisan correspondant à l'identifiant.
     */
    Optional<Artisan> findById(String id);

    /**
     * Recherche d'un artisan par son email.
     * 
     * @param email Adresse email de l'artisan.
     * @return Artisan correspondant à l'email.
     */
    Optional<Artisan> findByEmail(String email);

    /**
     * Recherche des artisans à proximité d'une localisation donnée.
     * Un index géospatial (`@GeoSpatialIndexed`) doit être défini sur `location` dans `Artisan`.
     * 
     * @param location Point de référence (latitude, longitude).
     * @param distance Rayon de recherche.
     * @return Liste des artisans dans la zone spécifiée.
     */
    List<Artisan> findByLocationNear(Point location, Distance distance);

    /**
     * Recherche des artisans en fonction de leur profession.
     * Insensible à la casse.
     * 
     * @param profession Nom de la profession.
     * @return Liste des artisans ayant cette profession.
     */
    List<Artisan> findByProfessionIgnoreCase(String profession);

    /**
     * Recherche des artisans par nom d'entreprise.
     * Insensible à la casse et permet une correspondance partielle.
     * 
     * @param companyName Nom (ou partie du nom) de l'entreprise.
     * @return Liste des artisans correspondant au critère.
     */
    List<Artisan> findByCompanyNameContainingIgnoreCase(String companyName);

    /**
     * Recherche des artisans par catégorie de services.
     * 
     * @param category Nom de la catégorie de service.
     * @return Liste des artisans proposant ce service.
     */
    List<Artisan> findByServiceCategoriesContainingIgnoreCase(String category);

    /**
     * Recherche avancée : artisans par profession et localisation.
     * 
     * @param profession Profession de l'artisan (ex: Plombier, Électricien).
     * @param location   Point de référence (latitude, longitude).
     * @param distance   Rayon de recherche.
     * @return Liste des artisans correspondant aux critères.
     */
    @Query("{ 'profession': { $regex: ?0, $options: 'i' }, 'location': { $near: { $geometry: ?1, $maxDistance: ?2 } } }")
    List<Artisan> findByProfessionAndLocation(String profession, Point location, double distance);

    /**
     * Recherche paginée des artisans pour améliorer les performances sur de grandes bases de données.
     * 
     * @param pageable Paramètre pour la pagination (taille, tri, etc.).
     * @return Page contenant une liste d'artisans.
     */
    Page<Artisan> findAll(Pageable pageable);
}
