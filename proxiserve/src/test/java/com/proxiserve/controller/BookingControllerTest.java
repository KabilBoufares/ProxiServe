package com.proxiserve.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proxiserve.model.*;
import com.proxiserve.repository.*;
import com.proxiserve.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.AfterEach;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
public class BookingControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtTokenProvider jwtTokenProvider;
    @Autowired private UserRepository userRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private ArtisanRepository artisanRepository;
    @Autowired private ServiceRepository serviceRepository;
    @Autowired private BookingRepository bookingRepository;

    private String jwtTokenClient;
    private String jwtTokenArtisan;
    private String clientId;
    private String artisanId;
    private String serviceId;
    private String bookingId;

    @BeforeEach
    public void setup() {
        bookingRepository.deleteAll();
        clientRepository.deleteAll();
        artisanRepository.deleteAll();
        serviceRepository.deleteAll();
        userRepository.deleteAll();

        // Client
        User clientUser = new User();
        clientUser.setEmail("client@test.com");
        clientUser.setPassword("dummy");
        clientUser.setRole("ROLE_CLIENT");
        clientUser = userRepository.save(clientUser);

        Client client = new Client();
        client.setUserId(clientUser.getId());
        client.setFullName("Client Test");
        client.setEmail(clientUser.getEmail());
        clientId = clientRepository.save(client).getId();

        Authentication clientAuth = new UsernamePasswordAuthenticationToken(
                clientUser.getEmail(), null,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));
        jwtTokenClient = jwtTokenProvider.generateToken(clientAuth);

        // Artisan
        User artisanUser = new User();
        artisanUser.setEmail("artisan@test.com");
        artisanUser.setPassword("dummy");
        artisanUser.setRole("ROLE_ARTISAN");
        artisanUser = userRepository.save(artisanUser);

        Artisan artisan = new Artisan();
        artisan.setEmail(artisanUser.getEmail());
        artisan.setProfession("Plombier");
        artisan.setUserId(artisanUser.getId());
        artisanId = artisanRepository.save(artisan).getId();

        Authentication artisanAuth = new UsernamePasswordAuthenticationToken(
                artisanUser.getEmail(), null,
                List.of(new SimpleGrantedAuthority("ROLE_ARTISAN")));
        jwtTokenArtisan = jwtTokenProvider.generateToken(artisanAuth);

        // Service
        ServiceEntity service = new ServiceEntity();
        service.setTitle("Réparation");
        service.setDescription("Réparation fuite");
        service.setPrice(99.0);
        service.setArtisanId(artisanId);
        serviceId = serviceRepository.save(service).getId();

        // Booking (déjà enregistré pour tests confirm/reject/complete)
        Booking booking = new Booking();
        booking.setClientId(clientId);
        booking.setArtisanId(artisanId);
        booking.setServiceId(serviceId);
        booking.setStatus("PENDING");
        booking.setBookingDate(LocalDateTime.now().plusDays(3));
        booking.setCreatedAt(LocalDateTime.now());
        bookingId = bookingRepository.save(booking).getId();
    }

    @AfterEach
    public void cleanup() {
        bookingRepository.deleteAll();
        clientRepository.deleteAll();
        artisanRepository.deleteAll();
        serviceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testCreateBooking_withoutToken_shouldReturn403() throws Exception {
        Booking booking = new Booking();
        booking.setBookingDate(LocalDateTime.now().plusDays(2));
        booking.setClientId(clientId);
        booking.setArtisanId(artisanId);
        booking.setServiceId(serviceId);
        booking.setStatus("PENDING");

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(booking)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testCreateBooking_withValidToken_shouldReturn201() throws Exception {
        Booking booking = new Booking();
        booking.setBookingDate(LocalDateTime.now().plusDays(2));
        booking.setClientId(clientId);
        booking.setArtisanId(artisanId);
        booking.setServiceId(serviceId);
        booking.setStatus("PENDING");

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtTokenClient)
                        .content(objectMapper.writeValueAsString(booking)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.serviceId").value(serviceId))
                .andExpect(jsonPath("$.clientId").value(clientId));
    }

    @Test
    void testGetBookingsForClient_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/bookings/client")
                        .header("Authorization", "Bearer " + jwtTokenClient))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].clientFullName").value("Client Test"))

                .andExpect(jsonPath("$[0].serviceTitle").value("Réparation"));
    }

    @Test
    void testGetBookingsForArtisan_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/bookings/artisan")
                        .header("Authorization", "Bearer " + jwtTokenArtisan))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[0].serviceTitle").value("Réparation"))
                .andExpect(jsonPath("$[0].clientFullName").value("Client Test"));
    }

    @Test
    void testConfirmBooking_shouldReturn200() throws Exception {
        mockMvc.perform(put("/api/bookings/" + bookingId + "/confirm")
                        .header("Authorization", "Bearer " + jwtTokenArtisan))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Réservation confirmée avec succès"));
    }

    @Test
    void testRejectBooking_shouldReturn200() throws Exception {
        mockMvc.perform(put("/api/bookings/" + bookingId + "/reject")
                        .header("Authorization", "Bearer " + jwtTokenArtisan))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Réservation rejetée avec succès"));
    }

    @Test
    void testCompleteBooking_shouldReturn200() throws Exception {
        mockMvc.perform(put("/api/bookings/" + bookingId + "/confirm")
                        .header("Authorization", "Bearer " + jwtTokenArtisan))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/bookings/" + bookingId + "/complete")
                        .header("Authorization", "Bearer " + jwtTokenArtisan))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Réservation terminée avec succès"));
    }

    @Test
    void testCancelBooking_shouldReturn200() throws Exception {
        Booking booking = new Booking();
        booking.setClientId(clientId);
        booking.setArtisanId(artisanId);
        booking.setServiceId(serviceId);
        booking.setStatus("PENDING");
        booking.setBookingDate(LocalDateTime.now().plusDays(4));
        booking.setCreatedAt(LocalDateTime.now());
        String cancelId = bookingRepository.save(booking).getId();

        mockMvc.perform(delete("/api/bookings/" + cancelId)
                        .header("Authorization", "Bearer " + jwtTokenClient))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Réservation annulée avec succès"));
    }
}
