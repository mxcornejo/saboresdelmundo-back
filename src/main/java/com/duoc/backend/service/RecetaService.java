package com.duoc.backend.service;

import com.duoc.backend.dto.RecipeRequest;
import com.duoc.backend.entity.Recipe;
import com.duoc.backend.entity.User;
import com.duoc.backend.repository.RecipeRepository;
import com.duoc.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecetaService {

    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    public List<Recipe> getAll() {
        return recipeRepository.findAll();
    }

    public Optional<Recipe> getById(Long id) {
        return recipeRepository.findById(id);
    }

    public List<Recipe> getRecientes() {
        return recipeRepository.findByRecienteTrue();
    }

    public List<Recipe> getPopulares() {
        return recipeRepository.findAllByOrderByPopularidadDesc();
    }

    public List<Recipe> buscar(String nombre, String tipoCocina, String ingrediente,
            String pais, String dificultad) {
        String nombreFiltro = esVacio(nombre) ? null : nombre;
        String tipoFiltro = esVacio(tipoCocina) ? null : tipoCocina;
        String paisFiltro = esVacio(pais) ? null : pais;
        String dificultadFiltro = esVacio(dificultad) ? null : dificultad;

        List<Recipe> results = recipeRepository.buscar(nombreFiltro, tipoFiltro, paisFiltro, dificultadFiltro);

        if (!esVacio(ingrediente)) {
            String ingLower = ingrediente.toLowerCase();
            results = results.stream()
                    .filter(r -> r.getIngredientes().stream()
                            .anyMatch(i -> i.toLowerCase().contains(ingLower)))
                    .collect(Collectors.toList());
        }

        return results;
    }

    /** Retorna las recetas publicadas por el usuario autenticado. */
    public List<Recipe> getMisRecetas(String username) {
        User autor = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        return recipeRepository.findByAutorId(autor.getId());
    }

    /** Crea una nueva receta asociada al usuario autenticado. */
    @Transactional
    public Recipe createRecipe(RecipeRequest req, String username) {
        if (esVacio(req.getNombre())) {
            throw new IllegalArgumentException("El nombre de la receta es obligatorio.");
        }
        User autor = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));

        Recipe recipe = new Recipe();
        mapRequestToRecipe(req, recipe);
        recipe.setAutor(autor);
        recipe.setReciente(true);
        return recipeRepository.save(recipe);
    }

    /** Actualiza una receta existente. Solo el autor o un admin puede hacerlo. */
    @Transactional
    public Recipe updateRecipe(Long id, RecipeRequest req, String username, boolean isAdmin) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Receta no encontrada."));

        boolean isOwner = recipe.getAutor() != null
                && recipe.getAutor().getUsername().equals(username);
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("No tienes permiso para modificar esta receta.");
        }
        mapRequestToRecipe(req, recipe);
        return recipeRepository.save(recipe);
    }

    /** Elimina una receta. Solo el autor o un admin puede hacerlo. */
    @Transactional
    public void deleteRecipe(Long id, String username, boolean isAdmin) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Receta no encontrada."));

        boolean isOwner = recipe.getAutor() != null
                && recipe.getAutor().getUsername().equals(username);
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("No tienes permiso para eliminar esta receta.");
        }
        recipeRepository.deleteById(id);
    }

    /** Alterna el estado de favorito de una receta para el usuario autenticado. */
    @Transactional
    public boolean toggleFavorite(Long recetaId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        Recipe recipe = recipeRepository.findById(recetaId)
                .orElseThrow(() -> new IllegalArgumentException("Receta no encontrada."));

        if (user.getFavoriteRecipes().contains(recipe)) {
            user.getFavoriteRecipes().remove(recipe);
            userRepository.save(user);
            return false; // ya no es favorito
        } else {
            user.getFavoriteRecipes().add(recipe);
            userRepository.save(user);
            return true; // ahora es favorito
        }
    }

    /** Retorna las recetas favoritas del usuario autenticado. */
    public List<Recipe> getFavorites(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        return new ArrayList<>(user.getFavoriteRecipes());
    }

    private void mapRequestToRecipe(RecipeRequest req, Recipe recipe) {
        if (!esVacio(req.getNombre()))
            recipe.setNombre(req.getNombre());
        recipe.setTipoCocina(req.getTipoCocina());
        recipe.setPaisOrigen(req.getPaisOrigen());
        recipe.setDificultad(req.getDificultad());
        recipe.setTiempoCoccion(req.getTiempoCoccion());
        recipe.setDescripcionCorta(req.getDescripcionCorta());
        recipe.setDescripcion(req.getDescripcion());
        recipe.setImagenUrl(req.getImagenUrl());
        recipe.setVideoUrl(req.getVideoUrl());
        if (req.getIngredientes() != null)
            recipe.setIngredientes(req.getIngredientes());
        if (req.getInstrucciones() != null)
            recipe.setInstrucciones(req.getInstrucciones());
        if (req.getMediaUrls() != null)
            recipe.setMediaUrls(req.getMediaUrls());
    }

    private boolean esVacio(String s) {
        return s == null || s.isBlank();
    }
}
