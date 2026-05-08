package com.duoc.backend.dto;

import lombok.Data;

@Data
public class CommentRequest {
    private String contenido;
    private int calificacion; // 1-5
}
