package com.proxiserve.service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proxiserve.model.User;
import com.proxiserve.repository.UserRepository;

/**
 * Service pour gérer les tentatives de connexion et le verrouillage des comptes après plusieurs échecs.
 */
@Service
public class LoginAttemptService {

    private static final Logger logger = LoggerFactory.getLogger(LoginAttemptService.class);
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_TIME_DURATION = 15; // Minutes

    private final UserRepository userRepository;

    public LoginAttemptService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Incrémente le nombre d'échecs de connexion et verrouille le compte si nécessaire.
     * @param email Email de l'utilisateur.
     */
    @Transactional
    public void loginFailed(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            logger.warn(" Tentative de connexion avec un email inconnu : {}", email);
            return;
        }

        User user = userOpt.get();

        // Si le compte est déjà verrouillé, ne rien faire
        if (user.isAccountLocked()) {
            logger.warn(" Tentative de connexion sur un compte déjà verrouillé : {}", email);
            return;
        }

        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        logger.info(" Échec de connexion n°{} pour l'utilisateur : {}", user.getFailedLoginAttempts(), email);

        if (user.getFailedLoginAttempts() >= MAX_ATTEMPTS) {
            user.setAccountLocked(true);
            user.setLockTime(LocalDateTime.now());
            logger.warn(" Le compte de l'utilisateur {} est verrouillé pour {} minutes", email, LOCK_TIME_DURATION);
        }

        userRepository.save(user);
    }

    /**
     * Réinitialise le compteur d'échecs et déverrouille le compte après une connexion réussie.
     * @param email Email de l'utilisateur.
     */
    @Transactional
    public void loginSucceeded(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return;

        User user = userOpt.get();
        user.setFailedLoginAttempts(0);
        user.setAccountLocked(false);
        user.setLockTime(null);
        userRepository.save(user);

        logger.info(" Connexion réussie : le compte {} a été réinitialisé", email);
    }

    /**
     * Vérifie si un utilisateur est bloqué en raison de trop nombreuses tentatives de connexion échouées.
     * Si le temps de verrouillage est écoulé, le compte est automatiquement déverrouillé.
     * @param email Email de l'utilisateur.
     * @return `true` si le compte est bloqué, `false` sinon.
     */
    public boolean isBlocked(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return false;
    
        User user = userOpt.get();
    
        if (user.isAccountLocked()) {
            long minutesSinceLock = ChronoUnit.MINUTES.between(user.getLockTime(), LocalDateTime.now());
    
            if (minutesSinceLock >= 15) { // Déverrouillage après 15 minutes
                user.setAccountLocked(false);
                user.setFailedLoginAttempts(0);
                user.setLockTime(null);
                userRepository.save(user);
                logger.info("Le compte {} a été automatiquement déverrouillé après {} minutes", email, minutesSinceLock);
                return false;
            }
    
            logger.warn("Le compte {} est toujours verrouillé. Temps restant : {} minutes", email, 15 - minutesSinceLock);
            return true;
        }
    
        return false;
    }
    
}
