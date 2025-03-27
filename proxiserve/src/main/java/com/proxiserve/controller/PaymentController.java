package com.proxiserve.controller;

import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private APIContext apiContext;

    // 1. Créer un paiement PayPal
    @PostMapping("/create")
    public ResponseEntity<?> createPayment(@RequestParam String amount, @RequestParam String currency) {
        Map<String, Object> response = new HashMap<>();

        Amount amt = new Amount();
        amt.setCurrency(currency);
        amt.setTotal(amount);

        Transaction transaction = new Transaction();
        transaction.setDescription("Paiement réservation");
        transaction.setAmount(amt);

        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl("https://ff2a-41-225-219-158.ngrok-free.app/api/payments/cancel");
        redirectUrls.setReturnUrl("https://ff2a-41-225-219-158.ngrok-free.app/api/payments/success");

        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(Collections.singletonList(transaction));
        payment.setRedirectUrls(redirectUrls);

        try {
            Payment createdPayment = payment.create(apiContext);
            for (Links link : createdPayment.getLinks()) {
                if ("approval_url".equals(link.getRel())) {
                    response.put("redirect_url", link.getHref());
                }
            }
            response.put("payment_id", createdPayment.getId());
            return ResponseEntity.ok(response);
        } catch (PayPalRESTException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 2. Paiement annulé
    @GetMapping("/cancel")
    public ResponseEntity<?> cancelPayment() {
        return ResponseEntity.ok("Le paiement a été annulé.");
    }

    // 3. Paiement validé avec succès
    @GetMapping("/success")
    public ResponseEntity<?> successPayment(@RequestParam("paymentId") String paymentId,
                                            @RequestParam("PayerID") String payerId) {
        try {
            Payment payment = Payment.get(apiContext, paymentId);

            PaymentExecution paymentExecution = new PaymentExecution();
            paymentExecution.setPayerId(payerId);

            Payment executedPayment = payment.execute(apiContext, paymentExecution);

            return ResponseEntity.ok("Paiement effectué avec succès : " + executedPayment.getId());
        } catch (PayPalRESTException e) {
            return ResponseEntity.badRequest().body("Erreur lors de l’exécution du paiement : " + e.getMessage());
        }
    }
}
