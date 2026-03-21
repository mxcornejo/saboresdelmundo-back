package com.duoc.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recetas")
@Getter
@Setter
@NoArgsConstructor
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String tipoCocina;

    private String paisOrigen;

    private String dificultad;

    private int tiempoCoccion;

    @Column(length = 500)
    private String descripcionCorta;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "receta_ingredientes", joinColumns = @JoinColumn(name = "receta_id"))
    @Column(name = "ingrediente")
    @OrderColumn(name = "orden")
    private List<String> ingredientes = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "receta_instrucciones", joinColumns = @JoinColumn(name = "receta_id"))
    @Column(name = "instruccion", columnDefinition = "TEXT")
    @OrderColumn(name = "orden")
    private List<String> instrucciones = new ArrayList<>();

    private String imagenUrl;

    private int popularidad;

    private boolean reciente;
}
