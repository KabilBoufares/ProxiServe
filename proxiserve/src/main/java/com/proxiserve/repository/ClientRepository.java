package com.proxiserve.repository;

import com.proxiserve.model.Client;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import java.util.Optional;
import java.util.List;

/**
 * Repository pour gérer les clients dans MongoDB.
 */
@Repository
public interface ClientRepository extends MongoRepository<Client, String> {

    /**
     * Trouver un client par son ID utilisateur.
     * @param userId ID de l'utilisateur associé au client.
     * @return Un `Optional<Client>` si le client est trouvé, sinon vide.
     */
    Optional<Client> findByUserId(String userId);

    /**
     * Recherche d'un client par numéro de téléphone.
     * @param phoneNumber Numéro de téléphone du client.
     * @return Un `Optional<Client>` si un client avec ce numéro existe.
     */
    Optional<Client> findByPhoneNumber(String phoneNumber);

    /**
     * Recherche de clients dont le nom contient un certain mot-clé (recherche insensible à la casse).
     * @param fullName Mot-clé à rechercher dans le nom complet du client.
     * @return Liste des clients correspondant à la recherche.
     */
    List<Client> findByFullNameContainingIgnoreCase(String fullName);

    /**
     * Recherche avancée : clients créés après une certaine date.
     * @param createdAt Date limite (seuls les clients créés après cette date seront retournés).
     * @return Liste des clients récents.
     */
    @Query("{ 'createdAt': { $gte: ?0 } }")
    List<Client> findByCreatedAtAfter(String createdAt);

    /**
     * Recherche paginée des clients (utile pour de grandes bases de données).
     * @param pageable Paramètre de pagination (page, taille, tri).
     * @return Une page contenant une liste de clients.
     */
    Page<Client> findAll(Pageable pageable);
}
