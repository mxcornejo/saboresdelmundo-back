package com.duoc.backend.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RecipeTest {

    @Test
    void prePersist_createdAtNulo_setea() {
        Recipe recipe = new Recipe();
        assertThat(recipe.getCreatedAt()).isNull();

        recipe.prePersist();

        assertThat(recipe.getCreatedAt()).isNotNull();
        assertThat(recipe.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void prePersist_createdAtYaSetado_noSobreescribe() {
        Recipe recipe = new Recipe();
        LocalDateTime existente = LocalDateTime.of(2024, 1, 1, 0, 0);
        recipe.setCreatedAt(existente);

        recipe.prePersist();

        assertThat(recipe.getCreatedAt()).isEqualTo(existente);
    }
}
