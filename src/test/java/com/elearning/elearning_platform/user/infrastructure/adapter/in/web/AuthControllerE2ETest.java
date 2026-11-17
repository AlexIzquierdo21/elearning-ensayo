package com.elearning.elearning_platform.user.infrastructure.adapter.in.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.elearning.elearning_platform.user.domain.port.out.UserRepositoryPort;
import com.elearning.elearning_platform.user.infrastructure.adapter.in.web.dto.LoginRequest;
import com.elearning.elearning_platform.user.infrastructure.adapter.in.web.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
class AuthControllerE2ETest {

    @Autowired private MockMvc mockMvc;

    @Autowired private UserRepositoryPort userRepository;

    @Autowired private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // Clear database before each test
        userRepository.findAll().forEach(user -> userRepository.deleteById(user.getId().value()));
    }

    @Test
    void shouldRegisterAndLoginSuccessfully() throws Exception {
        // 1️⃣ Register
        RegisterRequest register =
                new RegisterRequest("e2e@test.com", "Password1", "John", "Doe", "STUDENT");

        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        // 2️⃣ Login
        LoginRequest login = new LoginRequest("e2e@test.com", "Password1");

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").isNumber());
    }

    @Test
    void shouldFailWhenRegisteringDuplicateEmail() throws Exception {
        RegisterRequest register =
                new RegisterRequest("duplicate@test.com", "Password1", "John", "Doe", "STUDENT");

        // First registration succeeds
        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        // Second registration with same email fails
        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldFailWhenLoginWithInvalidCredentials() throws Exception {
        LoginRequest login = new LoginRequest("nonexistent@test.com", "WrongPassword");

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldFailWhenLoginWithWrongPassword() throws Exception {
        RegisterRequest register =
                new RegisterRequest("wrongpass@test.com", "Password1", "John", "Doe", "STUDENT");

        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest("wrongpass@test.com", "WrongPassword1");

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }
}
