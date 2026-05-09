package com.duoc.backend.controller;

import com.duoc.backend.dto.LoginRequest;
import com.duoc.backend.dto.RegisterRequest;
import com.duoc.backend.entity.User;
import com.duoc.backend.exception.GlobalExceptionHandler;
import com.duoc.backend.repository.UserRepository;
import com.duoc.backend.security.JwtAuthFilter;
import com.duoc.backend.security.UserDetailsServiceImpl;
import com.duoc.backend.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockitoBean
        private AuthenticationManager authenticationManager;

        @MockitoBean
        private JwtService jwtService;

        @MockitoBean
        private UserDetailsServiceImpl userDetailsService;

        @MockitoBean
        private UserRepository userRepository;

        @MockitoBean
        private JwtAuthFilter jwtAuthFilter;

        @MockitoBean
        private PasswordEncoder passwordEncoder;

        @Test
        void login_credencialesValidas_retornaTokenY200() throws Exception {
                LoginRequest request = new LoginRequest("admin", "admin123");

                UserDetails userDetails = org.springframework.security.core.userdetails.User
                                .builder()
                                .username("admin")
                                .password("encoded")
                                .authorities("ROLE_ADMIN")
                                .build();

                User user = new User(1L, "admin", "encoded", "ROLE_ADMIN");

                when(authenticationManager.authenticate(any()))
                                .thenReturn(new UsernamePasswordAuthenticationToken("admin", null));
                when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
                when(jwtService.generateToken(userDetails)).thenReturn("test-jwt-token");
                when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                                .andExpect(jsonPath("$.type").value("Bearer"))
                                .andExpect(jsonPath("$.username").value("admin"))
                                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
        }

        @Test
        void login_credencialesInvalidas_retorna401() throws Exception {
                LoginRequest request = new LoginRequest("admin", "wrong");

                when(authenticationManager.authenticate(any()))
                                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.error").value("Credenciales inválidas"));
        }

        // ─── /api/auth/register ────────────────────────────────────────────────────

        @Test
        void register_datosValidos_retorna201() throws Exception {
                RegisterRequest req = new RegisterRequest();
                req.setUsername("nuevouser");
                req.setPassword("password123");
                req.setEmail("nuevo@test.com");
                req.setFullName("Nuevo Usuario");

                when(userRepository.existsByUsername("nuevouser")).thenReturn(false);
                when(userRepository.existsByEmail("nuevo@test.com")).thenReturn(false);
                when(passwordEncoder.encode("password123")).thenReturn("encoded");

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isCreated())
                                .andExpect(content().string("Usuario registrado exitosamente."));
        }

        @Test
        void register_campoVacio_retorna400() throws Exception {
                RegisterRequest req = new RegisterRequest();
                req.setUsername("");
                req.setPassword("password123");
                req.setEmail("nuevo@test.com");
                req.setFullName("Nombre");

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("Todos los campos son obligatorios."));
        }

        @Test
        void register_usernameMuyCorto_retorna400() throws Exception {
                RegisterRequest req = new RegisterRequest();
                req.setUsername("ab");
                req.setPassword("password123");
                req.setEmail("nuevo@test.com");
                req.setFullName("Nombre");

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content()
                                                .string("El nombre de usuario debe tener entre 3 y 50 caracteres."));
        }

        @Test
        void register_passwordCorta_retorna400() throws Exception {
                RegisterRequest req = new RegisterRequest();
                req.setUsername("usuario");
                req.setPassword("pass");
                req.setEmail("nuevo@test.com");
                req.setFullName("Nombre");

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("La contraseña debe tener al menos 8 caracteres."));
        }

        @Test
        void register_emailInvalido_retorna400() throws Exception {
                RegisterRequest req = new RegisterRequest();
                req.setUsername("usuario");
                req.setPassword("password123");
                req.setEmail("emailinvalido");
                req.setFullName("Nombre");

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string("El correo electrónico no es válido."));
        }

        @Test
        void register_usernameExistente_retorna409() throws Exception {
                RegisterRequest req = new RegisterRequest();
                req.setUsername("admin");
                req.setPassword("password123");
                req.setEmail("admin@test.com");
                req.setFullName("Admin");

                when(userRepository.existsByUsername("admin")).thenReturn(true);

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isConflict())
                                .andExpect(content().string("El nombre de usuario ya está en uso."));
        }

        @Test
        void register_emailExistente_retorna409() throws Exception {
                RegisterRequest req = new RegisterRequest();
                req.setUsername("nuevouser");
                req.setPassword("password123");
                req.setEmail("existente@test.com");
                req.setFullName("Nombre");

                when(userRepository.existsByUsername("nuevouser")).thenReturn(false);
                when(userRepository.existsByEmail("existente@test.com")).thenReturn(true);

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isConflict())
                                .andExpect(content().string("El correo electrónico ya está registrado."));
        }

        @Test
        void register_usernameMuyLargo_retorna400() throws Exception {
                RegisterRequest req = new RegisterRequest();
                req.setUsername("u".repeat(51));
                req.setPassword("password123");
                req.setEmail("largo@test.com");
                req.setFullName("Nombre");

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isBadRequest())
                                .andExpect(content()
                                                .string("El nombre de usuario debe tener entre 3 y 50 caracteres."));
        }
}
