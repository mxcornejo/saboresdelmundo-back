package com.duoc.backend.service;

import com.duoc.backend.dto.CommentRequest;
import com.duoc.backend.dto.CommentResponse;
import com.duoc.backend.entity.Comment;
import com.duoc.backend.entity.CommentStatus;
import com.duoc.backend.entity.Recipe;
import com.duoc.backend.entity.User;
import com.duoc.backend.repository.CommentRepository;
import com.duoc.backend.repository.RecipeRepository;
import com.duoc.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private User usuario;
    private Recipe receta;
    private Comment comentario;

    @BeforeEach
    void setUp() {
        usuario = new User(1L, "chef", "pass", "ROLE_USER");
        usuario.setFullName("Chef Test");
        usuario.setEmail("chef@test.com");

        receta = new Recipe();
        receta.setId(10L);
        receta.setNombre("Paella");

        comentario = new Comment();
        comentario.setId(100L);
        comentario.setContenido("Muy buena receta");
        comentario.setCalificacion(5);
        comentario.setAutor(usuario);
        comentario.setReceta(receta);
        comentario.setEstado(CommentStatus.PENDIENTE);
    }

    // ─── getApprovedComments(Long recetaId) ────────────────────────────────────

    @Test
    void getApprovedComments_conRecetaId_retornaListaFiltrada() {
        when(commentRepository.findByRecetaIdAndEstado(10L, CommentStatus.APROBADO))
                .thenReturn(List.of(comentario));

        List<CommentResponse> result = commentService.getApprovedComments(10L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContenido()).isEqualTo("Muy buena receta");
        assertThat(result.get(0).getAutorUsername()).isEqualTo("chef");
        assertThat(result.get(0).getRecetaId()).isEqualTo(10L);
    }

    @Test
    void getApprovedComments_conRecetaId_listaVacia() {
        when(commentRepository.findByRecetaIdAndEstado(10L, CommentStatus.APROBADO))
                .thenReturn(List.of());

        List<CommentResponse> result = commentService.getApprovedComments(10L);

        assertThat(result).isEmpty();
    }

    // ─── getApprovedComments() (sin id, para admin) ────────────────────────────

    @Test
    void getApprovedComments_sinId_retornaListaAprobados() {
        when(commentRepository.findByEstado(CommentStatus.APROBADO))
                .thenReturn(List.of(comentario));

        List<CommentResponse> result = commentService.getApprovedComments();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContenido()).isEqualTo("Muy buena receta");
    }

    // ─── getPendingComments() ──────────────────────────────────────────────────

    @Test
    void getPendingComments_retornaComentariosPendientes() {
        when(commentRepository.findByEstado(CommentStatus.PENDIENTE))
                .thenReturn(List.of(comentario));

        List<CommentResponse> result = commentService.getPendingComments();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEstado()).isEqualTo("PENDIENTE");
    }

    // ─── createComment() ──────────────────────────────────────────────────────

    @Test
    void createComment_datosValidos_retornaRespuesta() {
        CommentRequest req = new CommentRequest();
        req.setContenido("Excelente receta!");
        req.setCalificacion(4);

        Comment savedComment = new Comment();
        savedComment.setId(200L);
        savedComment.setContenido("Excelente receta!");
        savedComment.setCalificacion(4);
        savedComment.setAutor(usuario);
        savedComment.setReceta(receta);
        savedComment.setEstado(CommentStatus.PENDIENTE);

        when(recipeRepository.findById(10L)).thenReturn(Optional.of(receta));
        when(userRepository.findByUsername("chef")).thenReturn(Optional.of(usuario));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        CommentResponse result = commentService.createComment(10L, req, "chef");

        assertThat(result.getContenido()).isEqualTo("Excelente receta!");
        assertThat(result.getCalificacion()).isEqualTo(4);
        assertThat(result.getAutorUsername()).isEqualTo("chef");
    }

    @ParameterizedTest
    @MethodSource("validacionesContenidoYCalificacion")
    void createComment_validacionInvalida_lanzaExcepcion(String contenido, int calificacion, String mensajeEsperado) {
        CommentRequest req = new CommentRequest();
        req.setContenido(contenido);
        req.setCalificacion(calificacion);

        assertThatThrownBy(() -> commentService.createComment(10L, req, "chef"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(mensajeEsperado);
    }

    static Stream<Arguments> validacionesContenidoYCalificacion() {
        return Stream.of(
                Arguments.of(null, 3, "no puede estar vacío"),
                Arguments.of("   ", 3, "no puede estar vacío"),
                Arguments.of("ok", 0, "calificación"),
                Arguments.of("ok", 6, "calificación"));
    }

    @Test
    void createComment_contenidoDemasiadoLargo_lanzaExcepcion() {
        CommentRequest req = new CommentRequest();
        req.setContenido("a".repeat(1001));
        req.setCalificacion(3);

        assertThatThrownBy(() -> commentService.createComment(10L, req, "chef"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1000 caracteres");
    }

    @Test
    void createComment_recetaNoExiste_lanzaExcepcion() {
        CommentRequest req = new CommentRequest();
        req.setContenido("Buena receta");
        req.setCalificacion(3);

        when(recipeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.createComment(99L, req, "chef"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Receta no encontrada");
    }

    @Test
    void createComment_usuarioNoExiste_lanzaExcepcion() {
        CommentRequest req = new CommentRequest();
        req.setContenido("Buena receta");
        req.setCalificacion(3);

        when(recipeRepository.findById(10L)).thenReturn(Optional.of(receta));
        when(userRepository.findByUsername("noexiste")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.createComment(10L, req, "noexiste"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    // ─── approveComment() ─────────────────────────────────────────────────────

    @Test
    void approveComment_comentarioExistente_cambiaEstadoAAprobado() {
        when(commentRepository.findById(100L)).thenReturn(Optional.of(comentario));
        when(commentRepository.save(any())).thenReturn(comentario);

        commentService.approveComment(100L);

        assertThat(comentario.getEstado()).isEqualTo(CommentStatus.APROBADO);
        verify(commentRepository).save(comentario);
    }

    @Test
    void approveComment_comentarioNoExiste_lanzaExcepcion() {
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.approveComment(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Comentario no encontrado");
    }

    // ─── rejectComment() ──────────────────────────────────────────────────────

    @Test
    void rejectComment_comentarioExistente_cambiaEstadoARechazado() {
        when(commentRepository.findById(100L)).thenReturn(Optional.of(comentario));
        when(commentRepository.save(any())).thenReturn(comentario);

        commentService.rejectComment(100L);

        assertThat(comentario.getEstado()).isEqualTo(CommentStatus.RECHAZADO);
        verify(commentRepository).save(comentario);
    }

    @Test
    void rejectComment_comentarioNoExiste_lanzaExcepcion() {
        when(commentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.rejectComment(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Comentario no encontrado");
    }

    // ─── deleteComment() ──────────────────────────────────────────────────────

    @Test
    void deleteComment_invocaDeleteById() {
        doNothing().when(commentRepository).deleteById(100L);

        commentService.deleteComment(100L);

        verify(commentRepository).deleteById(100L);
    }

    // ─── toResponse con autor/receta nulos ────────────────────────────────────

    @Test
    void getApprovedComments_comentarioSinAutorNiReceta_usaValoresPorDefecto() {
        Comment sinAutor = new Comment();
        sinAutor.setId(1L);
        sinAutor.setContenido("Texto");
        sinAutor.setCalificacion(3);
        sinAutor.setAutor(null);
        sinAutor.setReceta(null);
        sinAutor.setEstado(CommentStatus.APROBADO);

        when(commentRepository.findByRecetaIdAndEstado(10L, CommentStatus.APROBADO))
                .thenReturn(List.of(sinAutor));

        List<CommentResponse> result = commentService.getApprovedComments(10L);

        assertThat(result.get(0).getAutorUsername()).isEqualTo("Anónimo");
        assertThat(result.get(0).getAutorFullName()).isEmpty();
        assertThat(result.get(0).getRecetaId()).isNull();
        assertThat(result.get(0).getRecetaNombre()).isEmpty();
    }

    @Test
    void getApprovedComments_estadoNulo_retornaStringVacio() {
        Comment sinEstado = new Comment();
        sinEstado.setId(2L);
        sinEstado.setContenido("Ok");
        sinEstado.setCalificacion(2);
        sinEstado.setAutor(usuario);
        sinEstado.setReceta(receta);
        sinEstado.setEstado(null);

        when(commentRepository.findByRecetaIdAndEstado(10L, CommentStatus.APROBADO))
                .thenReturn(List.of(sinEstado));

        List<CommentResponse> result = commentService.getApprovedComments(10L);

        assertThat(result.get(0).getEstado()).isEmpty();
    }

    @Test
    void createComment_comentarioGuardadoConCreatedAt_mapeoEsCorrecto() {
        CommentRequest req = new CommentRequest();
        req.setContenido("Delicioso");
        req.setCalificacion(5);

        Comment saved = new Comment();
        saved.setId(300L);
        saved.setContenido("Delicioso");
        saved.setCalificacion(5);
        saved.setAutor(usuario);
        saved.setReceta(receta);
        saved.setEstado(CommentStatus.PENDIENTE);

        when(recipeRepository.findById(10L)).thenReturn(Optional.of(receta));
        when(userRepository.findByUsername("chef")).thenReturn(Optional.of(usuario));
        when(commentRepository.save(any(Comment.class))).thenReturn(saved);

        CommentResponse result = commentService.createComment(10L, req, "chef");

        assertThat(result.getId()).isEqualTo(300L);
        assertThat(result.getRecetaNombre()).isEqualTo("Paella");
        assertThat(result.getAutorFullName()).isEqualTo("Chef Test");
    }
}
