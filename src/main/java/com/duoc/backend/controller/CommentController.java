package com.duoc.backend.controller;

import com.duoc.backend.dto.CommentRequest;
import com.duoc.backend.dto.CommentResponse;
import com.duoc.backend.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recetas")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /** GET /api/recetas/{id}/comentarios — Comentarios aprobados (público) */
    @GetMapping("/{id}/comentarios")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.getApprovedComments(id));
    }

    /** POST /api/recetas/{id}/comentarios — Crear comentario (requiere JWT) */
    @PostMapping("/{id}/comentarios")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long id,
            @RequestBody CommentRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        CommentResponse response = commentService.createComment(id, request, currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
