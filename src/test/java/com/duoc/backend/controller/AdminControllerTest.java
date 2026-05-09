package com.duoc.backend.controller;

import com.duoc.backend.dto.CommentResponse;
import com.duoc.backend.entity.User;
import com.duoc.backend.repository.UserRepository;
import com.duoc.backend.security.JwtAuthFilter;
import com.duoc.backend.security.UserDetailsServiceImpl;
import com.duoc.backend.service.CommentService;
import com.duoc.backend.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private User crearUsuario(Long id, String username, boolean enabled) {
        User u = new User(id, username, "pass", "ROLE_USER");
        u.setFullName("Full " + username);
        u.setEmail(username + "@test.com");
        u.setEnabled(enabled);
        return u;
    }

    private CommentResponse crearCommentResponse(Long id, String estado) {
        return new CommentResponse(id, "Contenido", 4, "autor", "Autor Full",
                1L, "Receta", LocalDateTime.now(), estado);
    }

    // ─── GET /api/admin/usuarios ───────────────────────────────────────────────

    @Test
    void getAllUsers_retornaListaYStatus200() throws Exception {
        when(userRepository.findAll()).thenReturn(
                List.of(crearUsuario(1L, "alice", true), crearUsuario(2L, "bob", false)));

        mockMvc.perform(get("/api/admin/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].username").value("alice"))
                .andExpect(jsonPath("$[1].enabled").value(false));
    }

    @Test
    void getAllUsers_listaVacia_retornaArrayVacio() throws Exception {
        when(userRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ─── PATCH /api/admin/usuarios/{id}/toggle ─────────────────────────────────

    @Test
    void toggleUser_usuarioHabilitado_loDesactiva() throws Exception {
        User user = crearUsuario(1L, "alice", true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(patch("/api/admin/usuarios/1/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    void toggleUser_usuarioDesactivado_loActiva() throws Exception {
        User user = crearUsuario(2L, "bob", false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(patch("/api/admin/usuarios/2/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void toggleUser_usuarioNoExiste_lanzaExcepcion() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mockMvc.perform(patch("/api/admin/usuarios/99/toggle")))
                .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    // ─── GET /api/admin/comentarios/pendientes ─────────────────────────────────

    @Test
    void getPendingComments_retornaListaYStatus200() throws Exception {
        when(commentService.getPendingComments())
                .thenReturn(List.of(crearCommentResponse(1L, "PENDIENTE")));

        mockMvc.perform(get("/api/admin/comentarios/pendientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));
    }

    // ─── GET /api/admin/comentarios/aprobados ──────────────────────────────────

    @Test
    void getApprovedComments_retornaListaYStatus200() throws Exception {
        when(commentService.getApprovedComments())
                .thenReturn(List.of(crearCommentResponse(2L, "APROBADO")));

        mockMvc.perform(get("/api/admin/comentarios/aprobados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].estado").value("APROBADO"));
    }

    // ─── PATCH /api/admin/comentarios/{id}/aprobar ─────────────────────────────

    @Test
    void approveComment_retornaStatus200() throws Exception {
        doNothing().when(commentService).approveComment(1L);

        mockMvc.perform(patch("/api/admin/comentarios/1/aprobar"))
                .andExpect(status().isOk());

        verify(commentService).approveComment(1L);
    }

    // ─── PATCH /api/admin/comentarios/{id}/rechazar ────────────────────────────

    @Test
    void rejectComment_retornaStatus200() throws Exception {
        doNothing().when(commentService).rejectComment(1L);

        mockMvc.perform(patch("/api/admin/comentarios/1/rechazar"))
                .andExpect(status().isOk());

        verify(commentService).rejectComment(1L);
    }

    // ─── DELETE /api/admin/comentarios/{id} ────────────────────────────────────

    @Test
    void deleteComment_retornaStatus204() throws Exception {
        doNothing().when(commentService).deleteComment(1L);

        mockMvc.perform(delete("/api/admin/comentarios/1"))
                .andExpect(status().isNoContent());

        verify(commentService).deleteComment(1L);
    }
}
