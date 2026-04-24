package com.duoc.backend.service;

import com.duoc.backend.entity.Recipe;
import com.duoc.backend.repository.RecipeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecetaServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private RecetaService recetaService;

    private Recipe crearReceta(Long id, String nombre, boolean reciente, int popularidad) {
        Recipe r = new Recipe();
        r.setId(id);
        r.setNombre(nombre);
        r.setReciente(reciente);
        r.setPopularidad(popularidad);
        r.setIngredientes(Arrays.asList("sal", "pimienta"));
        return r;
    }

    @Test
    void getAll_retornaTodas() {
        List<Recipe> recetas = Arrays.asList(
                crearReceta(1L, "Paella", false, 80),
                crearReceta(2L, "Sushi", false, 90));
        when(recipeRepository.findAll()).thenReturn(recetas);

        List<Recipe> result = recetaService.getAll();

        assertThat(result).hasSize(2);
        verify(recipeRepository).findAll();
    }

    @Test
    void getAll_listaVacia_retornaVacia() {
        when(recipeRepository.findAll()).thenReturn(Collections.emptyList());

        List<Recipe> result = recetaService.getAll();

        assertThat(result).isEmpty();
    }

    @Test
    void getById_encontrado_retornaOptionalConReceta() {
        Recipe receta = crearReceta(1L, "Paella", false, 80);
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(receta));

        Optional<Recipe> result = recetaService.getById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("Paella");
    }

    @Test
    void getById_noEncontrado_retornaOptionalVacio() {
        when(recipeRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Recipe> result = recetaService.getById(99L);

        assertThat(result).isEmpty();
    }

    @Test
    void getRecientes_retornaRecetasRecientes() {
        List<Recipe> recientes = List.of(crearReceta(1L, "Ceviche", true, 75));
        when(recipeRepository.findByRecienteTrue()).thenReturn(recientes);

        List<Recipe> result = recetaService.getRecientes();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("Ceviche");
        verify(recipeRepository).findByRecienteTrue();
    }

    @Test
    void getPopulares_retornaOrdenadosPorPopularidad() {
        List<Recipe> populares = Arrays.asList(
                crearReceta(2L, "Sushi", false, 95),
                crearReceta(1L, "Paella", false, 80));
        when(recipeRepository.findAllByOrderByPopularidadDesc()).thenReturn(populares);

        List<Recipe> result = recetaService.getPopulares();

        assertThat(result.get(0).getNombre()).isEqualTo("Sushi");
        assertThat(result.get(0).getPopularidad()).isGreaterThan(result.get(1).getPopularidad());
    }

    @Test
    void buscar_sinFiltros_retornaTodasDelRepositorio() {
        List<Recipe> recetas = List.of(crearReceta(1L, "Paella", false, 80));
        when(recipeRepository.buscar(null, null, null, null)).thenReturn(recetas);

        List<Recipe> result = recetaService.buscar(null, null, null, null, null);

        assertThat(result).hasSize(1);
        verify(recipeRepository).buscar(null, null, null, null);
    }

    @Test
    void buscar_conNombre_pasaNombreAlRepositorio() {
        List<Recipe> recetas = List.of(crearReceta(1L, "Paella española", false, 80));
        when(recipeRepository.buscar("Paella", null, null, null)).thenReturn(recetas);

        List<Recipe> result = recetaService.buscar("Paella", null, null, null, null);

        assertThat(result).hasSize(1);
        verify(recipeRepository).buscar("Paella", null, null, null);
    }

    @Test
    void buscar_conIngredientePresente_filtraEnMemoria() {
        Recipe r = crearReceta(1L, "Pasta", false, 70);
        r.setIngredientes(Arrays.asList("tomate", "sal", "ajo"));
        when(recipeRepository.buscar(null, null, null, null)).thenReturn(List.of(r));

        List<Recipe> result = recetaService.buscar(null, null, "tomate", null, null);

        assertThat(result).hasSize(1);
    }

    @Test
    void buscar_ingredienteNoCoincide_retornaListaVacia() {
        Recipe r = crearReceta(1L, "Pasta", false, 70);
        r.setIngredientes(Arrays.asList("tomate", "sal"));
        when(recipeRepository.buscar(null, null, null, null)).thenReturn(List.of(r));

        List<Recipe> result = recetaService.buscar(null, null, "pimiento", null, null);

        assertThat(result).isEmpty();
    }

    @Test
    void buscar_cadenaVacia_tratadaComoNull() {
        when(recipeRepository.buscar(null, null, null, null)).thenReturn(Collections.emptyList());

        recetaService.buscar("", "   ", null, null, "  ");

        verify(recipeRepository).buscar(null, null, null, null);
    }

    @Test
    void buscar_conTodosLosFiltros_pasaFiltrosAlRepositorio() {
        when(recipeRepository.buscar("Sushi", "Japonesa", "Japón", "Fácil")).thenReturn(Collections.emptyList());

        recetaService.buscar("Sushi", "Japonesa", null, "Japón", "Fácil");

        verify(recipeRepository).buscar("Sushi", "Japonesa", "Japón", "Fácil");
    }
}
