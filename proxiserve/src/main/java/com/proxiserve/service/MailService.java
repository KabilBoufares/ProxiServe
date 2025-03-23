package com.proxiserve.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service pour l'envoi d'emails via SMTP.
 */
@Service
public class MailService {

    private static final Logger logger = LoggerFactory.getLogger(MailService.class);
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}") // Récupère l'email de l'expéditeur depuis application.properties
    private String fromEmail;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envoie un email avec les paramètres spécifiés.
     * @param to Destinataire de l'email.
     * @param subject Sujet de l'email.
     * @param body Corps du message.
     */
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom(fromEmail); // Définit l'expéditeur

            mailSender.send(message);
            logger.info(" Email envoyé avec succès à : {}", to);
        } catch (Exception e) {
            logger.error(" Échec de l'envoi de l'email à : {} | Erreur : {}", to, e.getMessage());
        }
    }
}
