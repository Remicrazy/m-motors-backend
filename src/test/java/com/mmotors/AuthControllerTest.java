package com.mmotors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmotors.dto.auth.LoginRequest;
import com.mmotors.dto.auth.RegisterRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AuthController — Tests d'intégration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private static String accessToken;

    @Test
    @Order(1)
    @DisplayName("POST /api/auth/register — 201 avec données valides")
    void register_returns201() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setNom("Martin"); req.setPrenom("Sophie");
        req.setEmail("sophie.martin@test.fr");
        req.setTelephone("0612345678");
        req.setPassword("MotDePasse123!");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.email").value("sophie.martin@test.fr"))
                .andExpect(jsonPath("$.role").value("CLIENT"));
    }

    @Test
    @Order(2)
    @DisplayName("POST /api/auth/register — 409 si email déjà utilisé")
    void register_duplicateEmail_returns409() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setNom("Autre"); req.setPrenom("User");
        req.setEmail("sophie.martin@test.fr");
        req.setTelephone("0600000000");
        req.setPassword("MotDePasse123!");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/auth/register — 400 si mot de passe trop court")
    void register_shortPassword_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setNom("Test"); req.setPrenom("User");
        req.setEmail("test2@test.fr");
        req.setTelephone("0600000000");
        req.setPassword("court");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    @DisplayName("POST /api/auth/login — 200 avec token JWT")
    void login_validCredentials_returns200() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("sophie.martin@test.fr");
        req.setPassword("MotDePasse123!");

        String response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        accessToken = objectMapper.readTree(response).get("accessToken").asText();
    }

    @Test
    @Order(5)
    @DisplayName("POST /api/auth/login — 401 avec mauvais mot de passe")
    void login_wrongPassword_returns401() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("sophie.martin@test.fr");
        req.setPassword("MauvaisMotDePasse!");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(6)
    @DisplayName("GET /api/auth/me — 200 avec token valide")
    void getMe_withValidToken_returns200() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("sophie.martin@test.fr"));
    }

    @Test
    @Order(7)
    @DisplayName("GET /api/auth/me — 401 sans token")
    void getMe_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(8)
    @DisplayName("GET /api/vehicles — 200 sans authentification")
    void getVehicles_public_returns200() throws Exception {
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(9)
    @DisplayName("POST /api/vehicles — 403 pour un CLIENT")
    void createVehicle_asClient_returns403() throws Exception {
        mockMvc.perform(post("/api/vehicles")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"marque\":\"Test\",\"modele\":\"Test\",\"annee\":2020," +
                        "\"km\":10000,\"prix\":5000,\"type\":\"ACHAT\"}"))
                .andExpect(status().isForbidden());
    }
}
