package com.duoc.backend.controller;

import com.duoc.backend.dto.CommentRequest;
import com.duoc.backend.dto.CommentResponse;
import com.duoc.backend.security.JwtAuthFilter;
import com.duoc.backend.security.UserDetailsServiceImpl;
import com.duoc.backend.service.CommentService;
import com.duoc.backend.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private CommentResponse crearResponse(Long id, String contenido) {
        return new CommentResponse(id, contenido, 5, "chef", "Chef Test",
                10L, "Paella", LocalDateTime.now(), "PENDIENTE");
    }

    @Test
    void getComments_retornaListaYStatus200() throws Exception {
        when(commentService.getApprovedComments(10L))
                .thenReturn(List.of(crearResponse(1L, "Muy buena")));

        mockMvc.perform(get("/api/recetas/10/comentarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].contenido").value("Muy buena"));
    }

    @Test
    void getComments_listaVacia_retornaArrayVacio() throws Exception {
        when(commentService.getApprovedComments(10L)).thenReturn(List.of());

        mockMvc.perform(get("/api/recetas/10/comentarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(username = "chef")
    void createComment_usuarioAutenticado_retorna201() throws Exception {
        CommentRequest req = new CommentRequest();
        req.setContenido("Excelente!");
        req.setCalificacion(5);

        CommentResponse resp = crearResponse(99L, "Excelente!");

        when(commentService.createComment(eq(10L), any(CommentRequest.class), eq("chef")))
                .thenReturn(resp);

        mockMvc.perform(post("/api/recetas/10/comentarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contenido").value("Excelente!"))
                .andExpect(jsonPath("$.calificacion").value(5));
    }
}
