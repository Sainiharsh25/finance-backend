package com.finance.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.backend.dto.request.LoginRequest;
import com.finance.backend.dto.response.AuthResponse;
import com.finance.backend.dto.response.ApiResponse;
import com.finance.backend.enums.Role;
import com.finance.backend.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private com.finance.backend.util.JwtUtils jwtUtils;

    @MockBean
    private com.finance.backend.security.JwtAuthFilter jwtAuthFilter;


    @Test
    @DisplayName("login: returns 200 and token on valid credentials")
    void login_Success() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@finance.com");
        request.setPassword("admin123");

        AuthResponse authResponse = AuthResponse.builder()
                .token("mock.jwt.token")
                .type("Bearer")
                .userId(1L)
                .email("admin@finance.com")
                .fullName("System Admin")
                .role(Role.ADMIN)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("mock.jwt.token"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    @DisplayName("login: returns 401 on bad credentials")
    void login_BadCredentials() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@finance.com");
        request.setPassword("wrongpassword");

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("login: returns 400 when email is blank")
    void login_BlankEmail_Returns400() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("login: returns 400 when email format is invalid")
    void login_InvalidEmail_Returns400() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("not-an-email");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("login: returns 400 when password is blank")
    void login_BlankPassword_Returns400() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@finance.com");
        request.setPassword("");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
