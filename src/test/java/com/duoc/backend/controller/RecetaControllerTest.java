package com.duoc.backend.controller;

import com.duoc.backend.dto.RecipeRequest;
import com.duoc.backend.entity.Recipe;
import com.duoc.backend.security.JwtAuthFilter;
import com.duoc.backend.security.UserDetailsServiceImpl;
import com.duoc.backend.service.JwtService;
import com.duoc.backend.service.RecetaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecetaController.class)
@AutoConfigureMockMvc(addFilters = false)
class RecetaControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

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

        private RecipeRequest crearRequest(String nombre) {
                RecipeRequest req = new RecipeRequest();
                req.setNombre(nombre);
                req.setTipoCocina("Italiana");
                return req;
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

        // ─── Endpoints autenticados ──────────────────────────────────────────────────

        @Test
        @WithMockUser(username = "chef")
        void getMisRecetas_usuarioAutenticado_retornaLista() throws Exception {
                when(recetaService.getMisRecetas("chef"))
                                .thenReturn(List.of(crearReceta(1L, "Mi Pizza")));

                mockMvc.perform(get("/api/recetas/mis-recetas"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].nombre").value("Mi Pizza"));
        }

        @Test
        @WithMockUser(username = "chef")
        void createRecipe_requestValido_retorna201() throws Exception {
                RecipeRequest req = crearRequest("Tacos");
                Recipe created = crearReceta(5L, "Tacos");

                when(recetaService.createRecipe(any(RecipeRequest.class), eq("chef"))).thenReturn(created);

                mockMvc.perform(post("/api/recetas")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.nombre").value("Tacos"));
        }

        @Test
        @WithMockUser(username = "chef")
        void updateRecipe_requestValido_retorna200() throws Exception {
                RecipeRequest req = crearRequest("Tacos Mejorados");
                Recipe updated = crearReceta(5L, "Tacos Mejorados");

                when(recetaService.updateRecipe(eq(5L), any(RecipeRequest.class), eq("chef"), anyBoolean()))
                                .thenReturn(updated);

                mockMvc.perform(put("/api/recetas/5")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.nombre").value("Tacos Mejorados"));
        }

        @Test
        @WithMockUser(username = "chef")
        void deleteRecipe_recetaExistente_retorna204() throws Exception {
                doNothing().when(recetaService).deleteRecipe(eq(5L), eq("chef"), anyBoolean());

                mockMvc.perform(delete("/api/recetas/5"))
                                .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(username = "chef")
        void toggleFavorite_agreagarFavorito_retornaTrue() throws Exception {
                when(recetaService.toggleFavorite(10L, "chef")).thenReturn(true);

                mockMvc.perform(post("/api/recetas/10/favorito"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.favorito").value(true));
        }

        @Test
        @WithMockUser(username = "chef")
        void getFavoritos_usuarioAutenticado_retornaLista() throws Exception {
                when(recetaService.getFavorites("chef"))
                                .thenReturn(List.of(crearReceta(10L, "Sushi Favorito")));

                mockMvc.perform(get("/api/recetas/favoritos"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].nombre").value("Sushi Favorito"));
        }
}
