package com.duoc.backend.controller;

import com.duoc.backend.entity.Recipe;
import com.duoc.backend.service.RecetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Todos los endpoints de este controlador son PRIVADOS.
 * Se requiere un token JWT válido en la cabecera: Authorization: Bearer <token>
 */
@RestController
@RequestMapping("/api/recetas")
@RequiredArgsConstructor
public class RecetaController {

    private final RecetaService recetaService;

    /** GET /api/recetas — Todas las recetas */
    @GetMapping
    public ResponseEntity<List<Recipe>> getAll() {
        return ResponseEntity.ok(recetaService.getAll());
    }

    /** GET /api/recetas/{id} — Receta por ID */
    @GetMapping("/{id}")
    public ResponseEntity<Recipe> getById(@PathVariable Long id) {
        return recetaService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** GET /api/recetas/recientes — Recetas marcadas como recientes */
    @GetMapping("/recientes")
    public ResponseEntity<List<Recipe>> getRecientes() {
        return ResponseEntity.ok(recetaService.getRecientes());
    }

    /** GET /api/recetas/populares — Recetas ordenadas por popularidad desc */
    @GetMapping("/populares")
    public ResponseEntity<List<Recipe>> getPopulares() {
        return ResponseEntity.ok(recetaService.getPopulares());
    }

    /**
     * GET /api/recetas/buscar — Búsqueda con filtros opcionales:
     * ?nombre=&tipoCocina=&ingrediente=&pais=&dificultad=
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<Recipe>> buscar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String tipoCocina,
            @RequestParam(required = false) String ingrediente,
            @RequestParam(required = false) String pais,
            @RequestParam(required = false) String dificultad) {
        return ResponseEntity.ok(recetaService.buscar(nombre, tipoCocina, ingrediente, pais, dificultad));
    }
}
