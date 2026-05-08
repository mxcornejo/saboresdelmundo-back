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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    /** Retorna solo los comentarios aprobados de una receta (acceso público). */
    public List<CommentResponse> getApprovedComments(Long recetaId) {
        return commentRepository.findByRecetaIdAndEstado(recetaId, CommentStatus.APROBADO)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /** Crea un nuevo comentario en estado PENDIENTE. */
    @Transactional
    public CommentResponse createComment(Long recetaId, CommentRequest req, String username) {
        if (req.getContenido() == null || req.getContenido().isBlank()) {
            throw new IllegalArgumentException("El contenido del comentario no puede estar vacío.");
        }
        if (req.getContenido().length() > 1000) {
            throw new IllegalArgumentException("El comentario no puede superar los 1000 caracteres.");
        }
        if (req.getCalificacion() < 1 || req.getCalificacion() > 5) {
            throw new IllegalArgumentException("La calificación debe estar entre 1 y 5.");
        }

        Recipe receta = recipeRepository.findById(recetaId)
                .orElseThrow(() -> new IllegalArgumentException("Receta no encontrada."));
        User autor = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        Comment comment = new Comment();
        comment.setContenido(req.getContenido());
        comment.setCalificacion(req.getCalificacion());
        comment.setAutor(autor);
        comment.setReceta(receta);

        return toResponse(commentRepository.save(comment));
    }

    /** Retorna todos los comentarios aprobados (moderación admin). */
    public List<CommentResponse> getApprovedComments() {
        return commentRepository.findByEstado(CommentStatus.APROBADO)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /** Retorna todos los comentarios pendientes (moderación). */
    public List<CommentResponse> getPendingComments() {
        return commentRepository.findByEstado(CommentStatus.PENDIENTE)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /** Aprueba un comentario. Solo admin. */
    @Transactional
    public void approveComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Comentario no encontrado."));
        comment.setEstado(CommentStatus.APROBADO);
        commentRepository.save(comment);
    }

    /** Rechaza un comentario. Solo admin. */
    @Transactional
    public void rejectComment(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Comentario no encontrado."));
        comment.setEstado(CommentStatus.RECHAZADO);
        commentRepository.save(comment);
    }

    /** Elimina un comentario. Solo admin. */
    @Transactional
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    private CommentResponse toResponse(Comment c) {
        return new CommentResponse(
                c.getId(),
                c.getContenido(),
                c.getCalificacion(),
                c.getAutor() != null ? c.getAutor().getUsername() : "Anónimo",
                c.getAutor() != null ? c.getAutor().getFullName() : "",
                c.getReceta() != null ? c.getReceta().getId() : null,
                c.getReceta() != null ? c.getReceta().getNombre() : "",
                c.getCreatedAt(),
                c.getEstado() != null ? c.getEstado().name() : "");
    }
}
