package com.duoc.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
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

    private String videoUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "receta_media", joinColumns = @JoinColumn(name = "receta_id"))
    @Column(name = "media_url")
    private List<String> mediaUrls = new ArrayList<>();

    private int popularidad;

    private boolean reciente;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "autor_id", nullable = true)
    @JsonIgnoreProperties({ "password", "enabled", "favoriteRecipes", "role" })
    private User autor;

    @Column(nullable = true, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
