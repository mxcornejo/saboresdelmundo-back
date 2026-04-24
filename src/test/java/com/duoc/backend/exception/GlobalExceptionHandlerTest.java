package com.duoc.backend.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBadCredentials_retornaStatus401() {
        BadCredentialsException ex = new BadCredentialsException("Credenciales inválidas");

        ResponseEntity<Map<String, String>> response = handler.handleBadCredentials(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void handleBadCredentials_cuerpoContieneClaveError() {
        BadCredentialsException ex = new BadCredentialsException("Credenciales inválidas");

        ResponseEntity<Map<String, String>> response = handler.handleBadCredentials(ex);

        assertThat(response.getBody()).containsEntry("error", "Credenciales inválidas");
    }

    @Test
    void handleUsernameNotFound_retornaStatus401() {
        UsernameNotFoundException ex = new UsernameNotFoundException("Usuario no encontrado: fantasma");

        ResponseEntity<Map<String, String>> response = handler.handleUsernameNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void handleUsernameNotFound_cuerpoConMensajeDeExcepcion() {
        UsernameNotFoundException ex = new UsernameNotFoundException("Usuario no encontrado: fantasma");

        ResponseEntity<Map<String, String>> response = handler.handleUsernameNotFound(ex);

        assertThat(response.getBody()).containsEntry("error", "Usuario no encontrado: fantasma");
    }
}
