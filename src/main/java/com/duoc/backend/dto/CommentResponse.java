package com.duoc.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private String contenido;
    private int calificacion;
    private String autorUsername;
    private String autorFullName;
    private Long recetaId;
    private String recetaNombre;
    private LocalDateTime createdAt;
    private String estado;
}
