package com.duoc.backend.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommentTest {

    @Test
    void prePersist_estableceFechaYEstadoPendiente() {
        Comment comment = new Comment();
        comment.setContenido("Test");
        comment.setCalificacion(3);

        comment.prePersist();

        assertThat(comment.getCreatedAt()).isNotNull();
        assertThat(comment.getEstado()).isEqualTo(CommentStatus.PENDIENTE);
    }

    @Test
    void prePersist_createdAtEsAproximadamenteAhora() {
        Comment comment = new Comment();

        comment.prePersist();

        assertThat(comment.getCreatedAt()).isNotNull();
        assertThat(comment.getEstado()).isEqualTo(CommentStatus.PENDIENTE);
    }

    @Test
    void constructor_porDefecto_estadoNulo() {
        Comment comment = new Comment();

        assertThat(comment.getEstado()).isNull();
        assertThat(comment.getCreatedAt()).isNull();
    }
}
