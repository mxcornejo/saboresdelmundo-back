package com.duoc.backend.service;

import com.duoc.backend.entity.Recipe;
import com.duoc.backend.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecetaService {

    private final RecipeRepository recipeRepository;

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
        // Convertir cadenas vacías a null para que el filtro JPQL las ignore
        String nombreFiltro = esVacio(nombre) ? null : nombre;
        String tipoFiltro = esVacio(tipoCocina) ? null : tipoCocina;
        String paisFiltro = esVacio(pais) ? null : pais;
        String dificultadFiltro = esVacio(dificultad) ? null : dificultad;

        List<Recipe> results = recipeRepository.buscar(nombreFiltro, tipoFiltro, paisFiltro, dificultadFiltro);

        // Filtro adicional por ingrediente (colección secundaria)
        if (!esVacio(ingrediente)) {
            String ingLower = ingrediente.toLowerCase();
            results = results.stream()
                    .filter(r -> r.getIngredientes().stream()
                            .anyMatch(i -> i.toLowerCase().contains(ingLower)))
                    .collect(Collectors.toList());
        }

        return results;
    }

    private boolean esVacio(String s) {
        return s == null || s.isBlank();
    }
}
