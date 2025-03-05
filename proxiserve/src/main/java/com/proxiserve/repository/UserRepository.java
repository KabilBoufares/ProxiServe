package com.proxiserve.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.proxiserve.model.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    // Trouver un utilisateur par email
    Optional<User> findByEmail(String email);

    // Vérifier si un utilisateur existe avec un email donné
    boolean existsByEmail(String email);

    // Récupérer les utilisateurs par rôle (ex: "CLIENT", "ARTISAN", "ADMIN")
    List<User> findByRole(String role);
}
