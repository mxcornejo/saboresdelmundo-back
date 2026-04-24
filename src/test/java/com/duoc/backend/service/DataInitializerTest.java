package com.duoc.backend.service;

import com.duoc.backend.entity.Recipe;
import com.duoc.backend.entity.User;
import com.duoc.backend.repository.RecipeRepository;
import com.duoc.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private DataInitializer dataInitializer;

    @BeforeEach
    void configurarPasswords() {
        ReflectionTestUtils.setField(dataInitializer, "adminPassword", "admin123");
        ReflectionTestUtils.setField(dataInitializer, "chefPassword", "chef123");
        ReflectionTestUtils.setField(dataInitializer, "userPassword", "user123");
    }

    @Test
    void run_repositoriosVacios_guardaUsuariosYRecetas() {
        when(userRepository.count()).thenReturn(0L);
        when(recipeRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode(any())).thenReturn("$2a$encoded");
        when(userRepository.saveAll(any())).thenReturn(List.of(new User()));
        when(recipeRepository.saveAll(any())).thenReturn(List.of(new Recipe()));

        dataInitializer.run();

        verify(userRepository).saveAll(argThat(usuarios -> {
            List<?> lista = (List<?>) usuarios;
            return lista.size() == 3;
        }));
        verify(recipeRepository).saveAll(argThat(recetas -> {
            List<?> lista = (List<?>) recetas;
            return lista.size() == 6;
        }));
    }

    @Test
    void run_usuariosYaExisten_noGuardaUsuarios() {
        when(userRepository.count()).thenReturn(3L);
        when(recipeRepository.count()).thenReturn(6L);

        dataInitializer.run();

        verify(userRepository, never()).saveAll(any());
        verify(recipeRepository, never()).saveAll(any());
    }

    @Test
    void run_soloUsuariosVacios_soloGuardaUsuarios() {
        when(userRepository.count()).thenReturn(0L);
        when(recipeRepository.count()).thenReturn(6L);
        when(passwordEncoder.encode(any())).thenReturn("$2a$encoded");
        when(userRepository.saveAll(any())).thenReturn(List.of());

        dataInitializer.run();

        verify(userRepository).saveAll(any());
        verify(recipeRepository, never()).saveAll(any());
    }

    @Test
    void run_soloRecetasVacias_soloGuardaRecetas() {
        when(userRepository.count()).thenReturn(3L);
        when(recipeRepository.count()).thenReturn(0L);
        when(recipeRepository.saveAll(any())).thenReturn(List.of());

        dataInitializer.run();

        verify(userRepository, never()).saveAll(any());
        verify(recipeRepository).saveAll(any());
    }
}
