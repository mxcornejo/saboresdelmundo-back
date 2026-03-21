package com.duoc.backend.repository;

import com.duoc.backend.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findByRecienteTrue();

    List<Recipe> findAllByOrderByPopularidadDesc();

    /**
     * Búsqueda flexible con filtros opcionales. Un parámetro null omite ese filtro.
     * El filtro por ingrediente se aplica en la capa de servicio para manejar
     * la colección secundaria sin penalizar con joins costosos.
     */
    @Query("SELECT DISTINCT r FROM Recipe r WHERE " +
            "(:nombre IS NULL OR LOWER(r.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))) AND " +
            "(:tipoCocina IS NULL OR LOWER(r.tipoCocina) = LOWER(:tipoCocina)) AND " +
            "(:paisOrigen IS NULL OR LOWER(r.paisOrigen) = LOWER(:paisOrigen)) AND " +
            "(:dificultad IS NULL OR LOWER(r.dificultad) = LOWER(:dificultad))")
    List<Recipe> buscar(@Param("nombre") String nombre,
            @Param("tipoCocina") String tipoCocina,
            @Param("paisOrigen") String paisOrigen,
            @Param("dificultad") String dificultad);
}
