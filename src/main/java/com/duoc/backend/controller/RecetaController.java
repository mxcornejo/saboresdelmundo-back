package com.duoc.backend.controller;

import com.duoc.backend.dto.RecipeRequest;
import com.duoc.backend.entity.Recipe;
import com.duoc.backend.service.RecetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Endpoints de recetas. Los GETs son públicos; los de escritura requieren JWT.
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

    /** GET /api/recetas/buscar — Búsqueda con filtros opcionales */
    @GetMapping("/buscar")
    public ResponseEntity<List<Recipe>> buscar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String tipoCocina,
            @RequestParam(required = false) String ingrediente,
            @RequestParam(required = false) String pais,
            @RequestParam(required = false) String dificultad) {
        return ResponseEntity.ok(recetaService.buscar(nombre, tipoCocina, ingrediente, pais, dificultad));
    }

    /**
     * GET /api/recetas/mis-recetas — Recetas publicadas por el usuario autenticado
     */
    @GetMapping("/mis-recetas")
    public ResponseEntity<List<Recipe>> getMisRecetas(
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(recetaService.getMisRecetas(currentUser.getUsername()));
    }

    /** POST /api/recetas — Publicar nueva receta */
    @PostMapping
    public ResponseEntity<Recipe> createRecipe(
            @RequestBody RecipeRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        Recipe created = recetaService.createRecipe(request, currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /** PUT /api/recetas/{id} — Actualizar receta (dueño o admin) */
    @PutMapping("/{id}")
    public ResponseEntity<Recipe> updateRecipe(
            @PathVariable Long id,
            @RequestBody RecipeRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Recipe updated = recetaService.updateRecipe(id, request, currentUser.getUsername(), isAdmin);
        return ResponseEntity.ok(updated);
    }

    /** DELETE /api/recetas/{id} — Eliminar receta (dueño o admin) */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        recetaService.deleteRecipe(id, currentUser.getUsername(), isAdmin);
        return ResponseEntity.noContent().build();
    }

    /** POST /api/recetas/{id}/favorito — Alternar favorito */
    @PostMapping("/{id}/favorito")
    public ResponseEntity<Map<String, Object>> toggleFavorite(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        boolean isFavorite = recetaService.toggleFavorite(id, currentUser.getUsername());
        return ResponseEntity.ok(Map.of("favorito", isFavorite));
    }

    /** GET /api/recetas/favoritos — Recetas favoritas del usuario autenticado */
    @GetMapping("/favoritos")
    public ResponseEntity<List<Recipe>> getFavoritos(
            @AuthenticationPrincipal UserDetails currentUser) {
        return ResponseEntity.ok(recetaService.getFavorites(currentUser.getUsername()));
    }
}
