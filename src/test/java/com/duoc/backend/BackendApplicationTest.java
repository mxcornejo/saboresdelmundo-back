package com.duoc.backend;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BackendApplicationTest {

    @Test
    void instancia_noEsNula() {
        assertThat(new BackendApplication()).isNotNull();
    }

    @Test
    void main_invocaSpringApplicationRun() {
        try (MockedStatic<SpringApplication> mockedSA = mockStatic(SpringApplication.class)) {
            ConfigurableApplicationContext mockCtx = mock(ConfigurableApplicationContext.class);
            mockedSA.when(() -> SpringApplication.run(any(Class.class), any(String[].class)))
                    .thenReturn(mockCtx);

            BackendApplication.main(new String[] {});

            mockedSA.verify(() -> SpringApplication.run(BackendApplication.class, new String[] {}));
        }
    }
}
