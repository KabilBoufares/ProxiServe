package com.proxiserve.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import com.proxiserve.model.Booking;
import com.proxiserve.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Autowired;


@RestController
@RequestMapping("/api/stripe")
public class StripeWebhookController {

    @Autowired
    private BookingRepository bookingRepository;


    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
                                                      @RequestHeader("Stripe-Signature") String sigHeader) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().body("Signature non vérifiée !");
        }

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
            if (session != null) {
                String bookingId = session.getMetadata().get("bookingId");

                bookingRepository.findById(bookingId).ifPresent(booking -> {
                    booking.setPaymentStatus("PAID");
                    booking.setPaymentCompleted(true);
                    bookingRepository.save(booking);
                    System.out.println(" Réservation mise à jour : " + bookingId);
                });
                
            }
        }

        return ResponseEntity.ok("Webhook reçu.");
    }
}
