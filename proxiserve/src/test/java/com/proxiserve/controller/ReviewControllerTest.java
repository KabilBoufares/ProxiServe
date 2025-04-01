package com.proxiserve.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proxiserve.model.Review;
import com.proxiserve.model.User;
import com.proxiserve.repository.ReviewRepository;
import com.proxiserve.repository.UserRepository;
import com.proxiserve.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ReviewControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private JwtTokenProvider jwtTokenProvider;

    private String jwtTokenClient;
    private String userId;
    private String artisanId;
    private String bookingId;
    private String reviewId;

    @BeforeEach
    void setup() {
        reviewRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setEmail("client@test.com");
        user.setPassword("dummy");
        user.setRole("ROLE_CLIENT");
        user = userRepository.save(user);
        userId = user.getId();

        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getEmail(), null, List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));
        jwtTokenClient = jwtTokenProvider.generateToken(auth);

        artisanId = "artisan123";
        bookingId = "booking123";

        Review review = new Review();
        review.setUserId(userId);
        review.setArtisanId(artisanId);
        review.setBookingId(bookingId);
        review.setRating(5);
        review.setComment("Excellent travail");
        review.setCreatedAt(LocalDateTime.now());

        reviewId = reviewRepository.save(review).getId();
    }

    @Test
    void testAddReview_shouldReturn200() throws Exception {
        Review newReview = new Review();
        newReview.setArtisanId(artisanId);
        newReview.setBookingId("bookingNew");
        newReview.setRating(4);
        newReview.setComment("Très bon");

        mockMvc.perform(post("/api/reviews")
                .header("Authorization", "Bearer " + jwtTokenClient)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newReview)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment").value("Très bon"));
    }

    @Test
    void testGetReviewsByArtisan_shouldReturnReviewList() throws Exception {
        mockMvc.perform(get("/api/reviews/artisan/" + artisanId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clientName").value("Client inconnu"))
                .andExpect(jsonPath("$[0].rating").value(5));
    }

    @Test
    void testDeleteReview_shouldReturnSuccess() throws Exception {
        mockMvc.perform(delete("/api/reviews/" + reviewId)
                .header("Authorization", "Bearer " + jwtTokenClient))
                .andExpect(status().isOk())
                .andExpect(content().string("Avis supprimé avec succès."));
    }

    @Test
    void testDeleteReview_notFound() throws Exception {
        mockMvc.perform(delete("/api/reviews/fakeId")
                .header("Authorization", "Bearer " + jwtTokenClient))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Avis non trouvé"));
    }

    @Test
    void testDeleteReview_notOwner_shouldReturn403() throws Exception {
        User anotherUser = new User();
        anotherUser.setEmail("other@test.com");
        anotherUser.setPassword("dummy");
        anotherUser.setRole("ROLE_CLIENT");
        anotherUser = userRepository.save(anotherUser);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                anotherUser.getEmail(), null, List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));
        String otherToken = jwtTokenProvider.generateToken(auth);

        mockMvc.perform(delete("/api/reviews/" + reviewId)
                .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Non autorisé à supprimer cet avis."));
    }

    @Test
    void testGetStatsForArtisan_shouldReturnStats() throws Exception {
        mockMvc.perform(get("/api/reviews/stats/" + artisanId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artisanId").value(artisanId))
                .andExpect(jsonPath("$.averageRating").value(5.0))
                .andExpect(jsonPath("$.totalReviews").value(1));
    }
}
