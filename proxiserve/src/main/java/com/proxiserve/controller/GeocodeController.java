package com.proxiserve.controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/geocode")
public class GeocodeController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping
    public ResponseEntity<?> geocode(@RequestParam String city) {
        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
        String url = "https://nominatim.openstreetmap.org/search?format=json&q=" + encodedCity;

        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", "Proxiserve/1.0 (contact@proxiserve.tn)");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return ResponseEntity.ok().body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Erreur lors de la géolocalisation.");
        }
    }
}
