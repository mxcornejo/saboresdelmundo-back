package com.duoc.backend.controller;

import com.duoc.backend.entity.Recipe;
import com.duoc.backend.security.JwtAuthFilter;
import com.duoc.backend.security.UserDetailsServiceImpl;
import com.duoc.backend.service.JwtService;
import com.duoc.backend.service.RecetaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecetaController.class)
@AutoConfigureMockMvc(addFilters = false)
class RecetaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecetaService recetaService;

    /* Beans requeridos por SecurityConfig y JwtAuthFilter en el contexto web */
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private Recipe crearReceta(Long id, String nombre) {
        Recipe r = new Recipe();
        r.setId(id);
        r.setNombre(nombre);
        return r;
    }

    @Test
    void getAll_retornaListaYStatus200() throws Exception {
        when(recetaService.getAll()).thenReturn(
                Arrays.asList(crearReceta(1L, "Paella"), crearReceta(2L, "Sushi")));

        mockMvc.perform(get("/api/recetas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Paella"));
    }

    @Test
    void getById_recetaEncontrada_retorna200() throws Exception {
        when(recetaService.getById(1L)).thenReturn(Optional.of(crearReceta(1L, "Paella")));

        mockMvc.perform(get("/api/recetas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Paella"));
    }

    @Test
    void getById_recetaNoEncontrada_retorna404() throws Exception {
        when(recetaService.getById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/recetas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRecientes_retornaListaYStatus200() throws Exception {
        when(recetaService.getRecientes()).thenReturn(List.of(crearReceta(1L, "Ceviche")));

        mockMvc.perform(get("/api/recetas/recientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Ceviche"));
    }

    @Test
    void getPopulares_retornaListaYStatus200() throws Exception {
        when(recetaService.getPopulares()).thenReturn(
                Arrays.asList(crearReceta(2L, "Sushi"), crearReceta(1L, "Paella")));

        mockMvc.perform(get("/api/recetas/populares"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void buscar_conParametroNombre_retornaResultados() throws Exception {
        when(recetaService.buscar("Sushi", null, null, null, null))
                .thenReturn(List.of(crearReceta(1L, "Sushi Japones")));

        mockMvc.perform(get("/api/recetas/buscar").param("nombre", "Sushi"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void buscar_sinParametros_retornaListaVacia() throws Exception {
        when(recetaService.buscar(null, null, null, null, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/recetas/buscar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
