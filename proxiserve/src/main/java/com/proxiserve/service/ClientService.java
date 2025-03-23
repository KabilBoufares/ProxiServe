package com.proxiserve.service;

import com.proxiserve.model.Client;
import com.proxiserve.repository.ClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * Service pour la gestion des clients.
 * Fournit des méthodes pour récupérer, mettre à jour et supprimer des clients.
 */
@Service
public class ClientService {

    private static final Logger logger = LoggerFactory.getLogger(ClientService.class);
    private final ClientRepository clientRepository;

    /**
     * Injection du repository via le constructeur.
     * @param clientRepository Repository pour les clients.
     */
    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    /**
     * Récupérer tous les clients.
     * @return Liste de tous les clients.
     */
    public List<Client> getAllClients() {
        List<Client> clients = clientRepository.findAll();
        logger.info(" {} clients récupérés avec succès", clients.size());
        return clients;
    }

    /**
     * Récupérer un client par son ID.
     * @param id ID du client.
     * @return Client trouvé.
     * @throws IllegalArgumentException si le client n'existe pas.
     */
    public Client getClientById(String id) {
        logger.info("🔍 Recherche du client avec ID : {}", id);
        return clientRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn(" Client non trouvé avec ID : {}", id);
                    return new IllegalArgumentException("Client introuvable avec l'ID : " + id);
                });
    }

    /**
     * Mettre à jour un client.
     * @param id ID du client à mettre à jour.
     * @param updatedClient Nouvelles informations du client.
     * @return Client mis à jour.
     * @throws IllegalArgumentException si le client n'existe pas ou si les données sont invalides.
     */
    public Client updateClient(String id, Client updatedClient) {
        logger.info(" Mise à jour du client avec ID : {}", id);

        if (updatedClient == null) {
            logger.error(" Impossible de mettre à jour un client avec des données nulles !");
            throw new IllegalArgumentException("Les données du client mises à jour ne peuvent pas être nulles.");
        }

        Client existingClient = clientRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn(" Tentative de mise à jour d'un client inexistant avec ID : {}", id);
                    return new IllegalArgumentException("Client introuvable avec l'ID : " + id);
                });

        // Mise à jour des champs uniquement si les nouvelles valeurs ne sont pas nulles
        if (updatedClient.getFullName() != null) {
            existingClient.setFullName(updatedClient.getFullName());
        }
        if (updatedClient.getPhoneNumber() != null) {
            existingClient.setPhoneNumber(updatedClient.getPhoneNumber());
        }

        Client savedClient = clientRepository.save(existingClient);
        logger.info(" Client mis à jour avec succès : {}", savedClient.getFullName());
        return savedClient;
    }

    /**
     * Supprimer un client par son ID.
     * @param id ID du client à supprimer.
     * @return 
     * @throws IllegalArgumentException si le client n'existe pas.
     */
    public boolean deleteClient(String id) {
        logger.info(" Tentative de suppression du client avec ID : {}", id);

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn(" Tentative de suppression d'un client inexistant avec ID : {}", id);
                    return new IllegalArgumentException("Client introuvable avec l'ID : " + id);
                });

        clientRepository.delete(client);
        logger.info(" Client supprimé avec succès : {}", client.getFullName());
        return true;
    }
    
}
