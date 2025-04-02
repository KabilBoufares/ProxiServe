package com.proxiserve.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proxiserve.config.TestSecurityConfig;
import com.proxiserve.controller.ServiceSearchController;
import com.proxiserve.model.Artisan;
import com.proxiserve.model.ServiceEntity;
import com.proxiserve.repository.ArtisanRepository;
import com.proxiserve.repository.ServiceRepository;
import com.proxiserve.service.ArtisanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@Import(TestSecurityConfig.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ServiceSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServiceRepository serviceRepository;

    @MockBean
    private ArtisanRepository artisanRepository;

    @MockBean
    private ArtisanService artisanService;

    @Autowired
    private ObjectMapper objectMapper;

    private ServiceEntity service;
    private Artisan artisan;

    @BeforeEach
    void setUp() {
        service = new ServiceEntity();
        service.setId("service123");
        service.setTitle("Plomberie");
        service.setDescription("Réparation de fuite");
        service.setPrice(50.0);
        service.setArtisanId("artisan123");

        artisan = new Artisan();
        artisan.setId("artisan123");
        artisan.setEmail("artisan@test.com");
        artisan.setUserId("user123");
        artisan.setPhoneNumber("12345678");
        artisan.setProfession("Plombier");
        artisan.setCompanyName("Plomberie Express");
        artisan.setServiceCategories(List.of("Plomberie"));
        artisan.setLocation(new GeoJsonPoint(10.2, 36.8)); // (lon, lat)
    }

    @Test
    void testAdvancedSearch_shouldReturnMatchingService() throws Exception {
        when(serviceRepository.findAll()).thenReturn(List.of(service));
        when(artisanRepository.findById("artisan123")).thenReturn(java.util.Optional.of(artisan));
        when(artisanService.calculateAverageRating("artisan123")).thenReturn(4.5);

        mockMvc.perform(get("/api/services/search/advanced")
                        .param("query", "Plomberie")
                        .param("latitude", "36.8")
                        .param("longitude", "10.2")
                        .param("radiusKm", "5.0")
                        .param("sortBy", "rating"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Plomberie"))
                .andExpect(jsonPath("$[0].rating").value(4.5))
                .andExpect(jsonPath("$[0].distanceKm").value(0.0));
    }

    @Test
    void testAdvancedSearch_noMatch_shouldReturnEmpty() throws Exception {
        when(serviceRepository.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/api/services/search/advanced")
                        .param("latitude", "36.8")
                        .param("longitude", "10.2"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }



    @Test
    void testAdvancedSearch_sortByPrice_shouldReturnSortedResults() throws Exception {
        ServiceEntity service2 = new ServiceEntity("service124", "Electricité", "Installation électrique", 30.0, "artisan123");

        when(serviceRepository.findAll()).thenReturn(List.of(service, service2));
        when(artisanRepository.findById("artisan123")).thenReturn(java.util.Optional.of(artisan));
        when(artisanService.calculateAverageRating("artisan123")).thenReturn(4.0);

        mockMvc.perform(get("/api/services/search/advanced")
                .param("latitude", "36.8")
                .param("longitude", "10.2")
                .param("radiusKm", "5.0")
                .param("sortBy", "price"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Electricité"))
                .andExpect(jsonPath("$[1].title").value("Plomberie"));
    }

    @Test
    void testAdvancedSearch_pagination_shouldReturnLimitedResults() throws Exception {
        ServiceEntity service2 = new ServiceEntity("service124", "Electricité", "Installation électrique", 30.0, "artisan123");

        when(serviceRepository.findAll()).thenReturn(List.of(service, service2));
        when(artisanRepository.findById("artisan123")).thenReturn(java.util.Optional.of(artisan));
        when(artisanService.calculateAverageRating("artisan123")).thenReturn(4.0);

        mockMvc.perform(get("/api/services/search/advanced")
                .param("latitude", "36.8")
                .param("longitude", "10.2")
                .param("page", "0")
                .param("size", "1"))  // only 1 result per page
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testAdvancedSearch_withEmptyQuery_shouldReturnNearbyServices() throws Exception {
        when(serviceRepository.findAll()).thenReturn(List.of(service));
        when(artisanRepository.findById("artisan123")).thenReturn(java.util.Optional.of(artisan));
        when(artisanService.calculateAverageRating("artisan123")).thenReturn(4.5);

        mockMvc.perform(get("/api/services/search/advanced")
                .param("latitude", "36.8")
                .param("longitude", "10.2")
                .param("query", "")) // empty query
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Plomberie"));
    }
  

}
