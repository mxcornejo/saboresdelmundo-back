package com.duoc.backend.service;

import com.duoc.backend.entity.Recipe;
import com.duoc.backend.dto.RecipeRequest;
import com.duoc.backend.entity.User;
import com.duoc.backend.repository.RecipeRepository;
import com.duoc.backend.repository.UserRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecetaServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private UserRepository userRepository;

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

    private User crearUsuario(Long id, String username) {
        return new User(id, username, "pass", "ROLE_USER");
    }

    private RecipeRequest crearRequest(String nombre) {
        RecipeRequest req = new RecipeRequest();
        req.setNombre(nombre);
        req.setTipoCocina("Italiana");
        req.setPaisOrigen("Italia");
        req.setDificultad("Media");
        req.setDescripcion("Descripción de prueba");
        req.setIngredientes(List.of("harina", "agua"));
        req.setInstrucciones(List.of("Mezclar", "Hornear"));
        return req;
    }

    // ─── getMisRecetas ───────────────────────────────────────────────────────────

    @Test
    void getMisRecetas_usuarioEncontrado_retornaRecetas() {
        User autor = crearUsuario(1L, "chef");
        List<Recipe> recetas = List.of(crearReceta(10L, "Pizza", false, 70));

        when(userRepository.findByUsername("chef")).thenReturn(Optional.of(autor));
        when(recipeRepository.findByAutorId(1L)).thenReturn(recetas);

        List<Recipe> result = recetaService.getMisRecetas("chef");

        assertThat(result).hasSize(1);
        verify(recipeRepository).findByAutorId(1L);
    }

    @Test
    void getMisRecetas_usuarioNoEncontrado_lanzaExcepcion() {
        when(userRepository.findByUsername("desconocido")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recetaService.getMisRecetas("desconocido"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ─── createRecipe ────────────────────────────────────────────────────────────

    @Test
    void createRecipe_recetaValida_retornaRecetaGuardada() {
        User autor = crearUsuario(1L, "chef");
        RecipeRequest req = crearRequest("Pizza Margherita");
        Recipe saved = crearReceta(1L, "Pizza Margherita", true, 0);

        when(userRepository.findByUsername("chef")).thenReturn(Optional.of(autor));
        when(recipeRepository.save(any(Recipe.class))).thenReturn(saved);

        Recipe result = recetaService.createRecipe(req, "chef");

        assertThat(result.getNombre()).isEqualTo("Pizza Margherita");
        verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    void createRecipe_nombreVacio_lanzaExcepcion() {
        RecipeRequest req = crearRequest("");

        assertThatThrownBy(() -> recetaService.createRecipe(req, "chef"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre");
    }

    @Test
    void createRecipe_usuarioNoEncontrado_lanzaExcepcion() {
        RecipeRequest req = crearRequest("Pizza");
        when(userRepository.findByUsername("nadie")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recetaService.createRecipe(req, "nadie"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ─── updateRecipe ────────────────────────────────────────────────────────────

    @Test
    void updateRecipe_propietarioActualiza_exitoso() {
        User autor = crearUsuario(1L, "chef");
        Recipe existing = crearReceta(5L, "Pizza", false, 50);
        existing.setAutor(autor);
        RecipeRequest req = crearRequest("Pizza Mejorada");

        when(recipeRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(recipeRepository.save(any())).thenReturn(existing);

        Recipe result = recetaService.updateRecipe(5L, req, "chef", false);

        assertThat(result).isNotNull();
        verify(recipeRepository).save(existing);
    }

    @Test
    void updateRecipe_adminActualiza_exitoso() {
        User autor = crearUsuario(2L, "usuario");
        Recipe existing = crearReceta(5L, "Pizza", false, 50);
        existing.setAutor(autor);
        RecipeRequest req = crearRequest("Pizza Admin");

        when(recipeRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(recipeRepository.save(any())).thenReturn(existing);

        Recipe result = recetaService.updateRecipe(5L, req, "admin", true);

        assertThat(result).isNotNull();
        verify(recipeRepository).save(existing);
    }

    @Test
    void updateRecipe_sinPermiso_lanzaAccessDeniedException() {
        User autor = crearUsuario(2L, "otrousuario");
        Recipe existing = crearReceta(5L, "Pizza", false, 50);
        existing.setAutor(autor);
        RecipeRequest req = crearRequest("Pizza");

        when(recipeRepository.findById(5L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> recetaService.updateRecipe(5L, req, "chef", false))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }

    @Test
    void updateRecipe_recetaNoEncontrada_lanzaExcepcion() {
        RecipeRequest req = crearRequest("X");
        when(recipeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recetaService.updateRecipe(99L, req, "chef", false))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ─── deleteRecipe ────────────────────────────────────────────────────────────

    @Test
    void deleteRecipe_propietarioElimina_exitoso() {
        User autor = crearUsuario(1L, "chef");
        Recipe existing = crearReceta(5L, "Pizza", false, 50);
        existing.setAutor(autor);

        when(recipeRepository.findById(5L)).thenReturn(Optional.of(existing));

        recetaService.deleteRecipe(5L, "chef", false);

        verify(recipeRepository).deleteById(5L);
    }

    @Test
    void deleteRecipe_adminElimina_exitoso() {
        User autor = crearUsuario(2L, "otrousuario");
        Recipe existing = crearReceta(5L, "Pizza", false, 50);
        existing.setAutor(autor);

        when(recipeRepository.findById(5L)).thenReturn(Optional.of(existing));

        recetaService.deleteRecipe(5L, "admin", true);

        verify(recipeRepository).deleteById(5L);
    }

    @Test
    void deleteRecipe_sinPermiso_lanzaAccessDeniedException() {
        User autor = crearUsuario(2L, "otro");
        Recipe existing = crearReceta(5L, "Pizza", false, 50);
        existing.setAutor(autor);

        when(recipeRepository.findById(5L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> recetaService.deleteRecipe(5L, "chef", false))
                .isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
    }

    @Test
    void deleteRecipe_recetaNoEncontrada_lanzaExcepcion() {
        when(recipeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recetaService.deleteRecipe(99L, "chef", false))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ─── toggleFavorite ──────────────────────────────────────────────────────────

    @Test
    void toggleFavorite_agreagarFavorito_retornaTrue() {
        User user = crearUsuario(1L, "chef");
        Recipe recipe = crearReceta(10L, "Sushi", false, 80);

        when(userRepository.findByUsername("chef")).thenReturn(Optional.of(user));
        when(recipeRepository.findById(10L)).thenReturn(Optional.of(recipe));
        when(userRepository.save(user)).thenReturn(user);

        boolean result = recetaService.toggleFavorite(10L, "chef");

        assertThat(result).isTrue();
        assertThat(user.getFavoriteRecipes()).contains(recipe);
    }

    @Test
    void toggleFavorite_eliminarFavorito_retornaFalse() {
        User user = crearUsuario(1L, "chef");
        Recipe recipe = crearReceta(10L, "Sushi", false, 80);
        user.getFavoriteRecipes().add(recipe);

        when(userRepository.findByUsername("chef")).thenReturn(Optional.of(user));
        when(recipeRepository.findById(10L)).thenReturn(Optional.of(recipe));
        when(userRepository.save(user)).thenReturn(user);

        boolean result = recetaService.toggleFavorite(10L, "chef");

        assertThat(result).isFalse();
        assertThat(user.getFavoriteRecipes()).doesNotContain(recipe);
    }

    @Test
    void toggleFavorite_usuarioNoEncontrado_lanzaExcepcion() {
        when(userRepository.findByUsername("nadie")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recetaService.toggleFavorite(1L, "nadie"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ─── getFavorites
    // ─────────────────────────────────────────────────────────────

    @Test
    void getFavorites_usuarioConFavoritos_retornaLista() {
        User user = crearUsuario(1L, "chef");
        Recipe recipe = crearReceta(10L, "Paella", false, 90);
        user.getFavoriteRecipes().add(recipe);

        when(userRepository.findByUsername("chef")).thenReturn(Optional.of(user));

        List<Recipe> result = recetaService.getFavorites("chef");

        assertThat(result).hasSize(1);
    }

    @Test
    void getFavorites_usuarioNoEncontrado_lanzaExcepcion() {
        when(userRepository.findByUsername("nadie")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recetaService.getFavorites("nadie"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
