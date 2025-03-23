package com.proxiserve.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import com.proxiserve.model.User;

/**
 * Repository pour gérer les utilisateurs dans MongoDB.
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    /**
     * Trouver un utilisateur par email.
     * @param email Email de l'utilisateur.
     * @return Un `Optional<User>` contenant l'utilisateur s'il existe.
     */
    Optional<User> findByEmail(String email);

    /**
     * Vérifier si un utilisateur existe avec un email donné.
     * @param email Email à vérifier.
     * @return `true` si l'utilisateur existe, sinon `false`.
     */
    boolean existsByEmail(String email);

    /**
     * Récupérer les utilisateurs par rôle (ex: "CLIENT", "ARTISAN", "ADMIN").
     * @param role Rôle de l'utilisateur.
     * @return Liste des utilisateurs correspondant au rôle.
     */
    List<User> findByRole(String role);

    /**
     * Récupérer les utilisateurs par rôle, insensible à la casse.
     * @param role Rôle de l'utilisateur.
     * @return Liste des utilisateurs correspondant au rôle.
     */
    List<User> findByRoleIgnoreCase(String role);

    /**
     * Récupérer les utilisateurs créés après une certaine date.
     * @param createdAt Date limite.
     * @return Liste des utilisateurs récents.
     */
    @Query("{ 'createdAt': { $gte: ?0 } }")
    List<User> findByCreatedAtAfter(LocalDateTime createdAt);

    /**
     * Récupérer les utilisateurs avec pagination en fonction de leur rôle.
     * @param role Rôle de l'utilisateur.
     * @param pageable Objet de pagination.
     * @return Une page contenant les utilisateurs correspondants.
     */
    Page<User> findByRole(String role, Pageable pageable);
}
