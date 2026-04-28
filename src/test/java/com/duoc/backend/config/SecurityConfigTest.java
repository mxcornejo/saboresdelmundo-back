package com.duoc.backend.config;

import com.duoc.backend.repository.RecipeRepository;
import com.duoc.backend.repository.UserRepository;
import com.duoc.backend.security.JwtAuthFilter;
import com.duoc.backend.security.UserDetailsServiceImpl;
import com.duoc.backend.service.JwtService;
import com.duoc.backend.service.RecetaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Prueba de integración ligera para verificar la configuración de seguridad del
 * backend.
 * Al cargar el contexto de Spring MVC, se invoca
 * SecurityConfig.securityFilterChain()
 * cubriendo las instrucciones del paquete config.
 */
@WebMvcTest
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private RecipeRepository recipeRepository;

    @MockitoBean
    private RecetaService recetaService;

    @Test
    void apiRecetasGet_retorna200() throws Exception {
        when(recetaService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/recetas"))
                .andExpect(status().isOk());
    }

    @Test
    void apiRecetasRecientes_retorna200() throws Exception {
        when(recetaService.getRecientes()).thenReturn(List.of());

        mockMvc.perform(get("/api/recetas/recientes"))
                .andExpect(status().isOk());
    }

    @Test
    void passwordEncoder_retornaBCryptPasswordEncoder() {
        JwtAuthFilter mockFilter = mock(JwtAuthFilter.class);
        UserDetailsServiceImpl mockUds = mock(UserDetailsServiceImpl.class);
        SecurityConfig config = new SecurityConfig(mockFilter, mockUds);

        PasswordEncoder encoder = config.passwordEncoder();

        assertThat(encoder).isInstanceOf(BCryptPasswordEncoder.class);
        assertThat(encoder.matches("test123", encoder.encode("test123"))).isTrue();
    }

    @Test
    void corsConfigurationSource_retornaConfiguracionValida() {
        JwtAuthFilter mockFilter = mock(JwtAuthFilter.class);
        UserDetailsServiceImpl mockUds = mock(UserDetailsServiceImpl.class);
        SecurityConfig config = new SecurityConfig(mockFilter, mockUds);

        CorsConfigurationSource source = config.corsConfigurationSource();

        assertThat(source).isNotNull();
    }

    @Test
    void authenticationProvider_retornaProviderNoNulo() {
        JwtAuthFilter mockFilter = mock(JwtAuthFilter.class);
        UserDetailsServiceImpl mockUds = mock(UserDetailsServiceImpl.class);
        SecurityConfig config = new SecurityConfig(mockFilter, mockUds);

        assertThat(config.authenticationProvider()).isNotNull();
    }

    @Test
    void authenticationManager_retornaManagerDesdeConfiguration() throws Exception {
        JwtAuthFilter mockFilter = mock(JwtAuthFilter.class);
        UserDetailsServiceImpl mockUds = mock(UserDetailsServiceImpl.class);
        SecurityConfig config = new SecurityConfig(mockFilter, mockUds);

        AuthenticationConfiguration mockConfig = mock(AuthenticationConfiguration.class);
        AuthenticationManager mockManager = mock(AuthenticationManager.class);
        when(mockConfig.getAuthenticationManager()).thenReturn(mockManager);

        assertThat(config.authenticationManager(mockConfig)).isEqualTo(mockManager);
    }
}
