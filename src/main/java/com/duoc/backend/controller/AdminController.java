package com.duoc.backend.controller;

import com.duoc.backend.dto.CommentResponse;
import com.duoc.backend.dto.UserDto;
import com.duoc.backend.entity.User;
import com.duoc.backend.repository.UserRepository;
import com.duoc.backend.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints de administración — solo accesibles con ROLE_ADMIN (protegido en
 * SecurityConfig).
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final CommentService commentService;

    /** GET /api/admin/usuarios — Listar todos los usuarios */
    @GetMapping("/usuarios")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userRepository.findAll().stream()
                .map(u -> new UserDto(u.getId(), u.getUsername(), u.getFullName(),
                        u.getEmail(), u.getRole(), u.isEnabled()))
                .toList();
        return ResponseEntity.ok(users);
    }

    /** PATCH /api/admin/usuarios/{id}/toggle — Habilitar/deshabilitar usuario */
    @PatchMapping("/usuarios/{id}/toggle")
    public ResponseEntity<UserDto> toggleUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
        return ResponseEntity.ok(new UserDto(user.getId(), user.getUsername(), user.getFullName(),
                user.getEmail(), user.getRole(), user.isEnabled()));
    }

    /**
     * GET /api/admin/comentarios/pendientes — Comentarios pendientes de moderación
     */
    @GetMapping("/comentarios/pendientes")
    public ResponseEntity<List<CommentResponse>> getPendingComments() {
        return ResponseEntity.ok(commentService.getPendingComments());
    }

    /** GET /api/admin/comentarios/aprobados — Comentarios aprobados */
    @GetMapping("/comentarios/aprobados")
    public ResponseEntity<List<CommentResponse>> getApprovedComments() {
        return ResponseEntity.ok(commentService.getApprovedComments());
    }

    /** PATCH /api/admin/comentarios/{id}/aprobar — Aprobar comentario */
    @PatchMapping("/comentarios/{id}/aprobar")
    public ResponseEntity<Void> approveComment(@PathVariable Long id) {
        commentService.approveComment(id);
        return ResponseEntity.ok().build();
    }

    /** PATCH /api/admin/comentarios/{id}/rechazar — Rechazar comentario */
    @PatchMapping("/comentarios/{id}/rechazar")
    public ResponseEntity<Void> rejectComment(@PathVariable Long id) {
        commentService.rejectComment(id);
        return ResponseEntity.ok().build();
    }

    /** DELETE /api/admin/comentarios/{id} — Eliminar comentario */
    @DeleteMapping("/comentarios/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
