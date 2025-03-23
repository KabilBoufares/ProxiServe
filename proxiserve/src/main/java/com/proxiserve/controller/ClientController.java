package com.proxiserve.controller;

import org.springframework.web.bind.annotation.*;
import com.proxiserve.model.Client;
import com.proxiserve.service.ClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * Contrôleur REST pour gérer les clients.
 * Fournit des endpoints sécurisés pour récupérer et gérer les clients.
 */
@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);
    private final ClientService clientService;

    /**
     * Injection de dépendance via le constructeur.
     * @param clientService Service pour gérer les clients.
     */
    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    /**
     * Récupérer tous les clients (accessible uniquement par un administrateur).
     * @return Liste des clients.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<Client>> getAllClients() {
        logger.info(" Récupération de tous les clients...");
        List<Client> clients = clientService.getAllClients();
        
        if (clients.isEmpty()) {
            logger.info(" Aucun client trouvé !");
            return ResponseEntity.noContent().build();
        }

        logger.info(" {} clients récupérés avec succès.", clients.size());
        return ResponseEntity.ok(clients);
    }

    /**
     * Récupérer un client par son ID.
     * @param id ID du client.
     * @return Informations du client.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_CLIENT') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Client> getClientById(@PathVariable String id) {
        logger.info(" Récupération du client avec ID : {}", id);
        Client client = clientService.getClientById(id);

        if (client == null) {
            logger.warn(" Client non trouvé avec ID : {}", id);
            return ResponseEntity.notFound().build();
        }

        logger.info(" Client trouvé : {}", client.getFullName());
        return ResponseEntity.ok(client);
    }

    /**
     * Mettre à jour les informations d'un client.
     * @param id ID du client.
     * @param updatedClient Nouvelles informations du client.
     * @return Client mis à jour.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_CLIENT') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Client> updateClient(@PathVariable String id, @RequestBody Client updatedClient) {
        if (updatedClient == null) {
            logger.warn(" Tentative de mise à jour avec un client null !");
            return ResponseEntity.badRequest().body(null);
        }

        logger.info(" Mise à jour du client avec ID : {}", id);
        Client client = clientService.updateClient(id, updatedClient);

        if (client == null) {
            logger.warn(" Client non trouvé pour mise à jour - ID : {}", id);
            return ResponseEntity.notFound().build();
        }

        logger.info(" Client mis à jour avec succès : {}", client.getFullName());
        return ResponseEntity.ok(client);
    }

    /**
     * Supprimer un client (réservé aux administrateurs).
     * @param id ID du client.
     * @return Confirmation de suppression.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> deleteClient(@PathVariable String id) {
        logger.info(" Tentative de suppression du client avec ID : {}", id);
        boolean deleted = clientService.deleteClient(id);

        if (!deleted) {
            logger.warn(" Échec de suppression : Client non trouvé - ID : {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Erreur : Client non trouvé !");
        }

        logger.info(" Client supprimé avec succès - ID : {}", id);
        return ResponseEntity.ok("Client supprimé avec succès !");
    }
}
