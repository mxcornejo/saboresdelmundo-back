package com.duoc.backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class RecipeRequest {
    private String nombre;
    private String tipoCocina;
    private String paisOrigen;
    private String dificultad;
    private int tiempoCoccion;
    private String descripcionCorta;
    private String descripcion;
    private List<String> ingredientes;
    private List<String> instrucciones;
    private String imagenUrl;
    private String videoUrl;
    private List<String> mediaUrls;
}
