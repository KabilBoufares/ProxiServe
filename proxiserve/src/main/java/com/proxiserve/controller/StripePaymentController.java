package com.proxiserve.controller;

import com.proxiserve.dto.PaymentRequestDTO;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stripe")
public class StripePaymentController {

    @PostMapping("/create-checkout-session")
    public ResponseEntity<?> createCheckoutSession(@RequestBody PaymentRequestDTO request) throws StripeException {
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("https://0208-41-225-219-158.ngrok-free.app/success?bookingId=" + request.getBookingId())
                .setCancelUrl("https://0208-41-225-219-158.ngrok-free.app/cancel")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(request.getCurrency())
                                                .setUnitAmount((long) (request.getAmount() * 100)) // amount en cents
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Réservation Proxiserve")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        Session session = Session.create(params);
        return ResponseEntity.ok(Map.of("url", session.getUrl()));
    }


    @GetMapping("/success")
    public ResponseEntity<?> paymentSuccess(@RequestParam String bookingId) {
        return ResponseEntity.ok("Paiement réussi pour la réservation : " + bookingId);
    }

    @GetMapping("/cancel")
    public ResponseEntity<?> paymentCancelled() {
        return ResponseEntity.ok("Paiement annulé.");
    }
}
