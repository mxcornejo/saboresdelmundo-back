package com.duoc.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private static final String SECRET = "test-secret-key-for-unit-testing-purposes-long-enough-256bits";
    private static final long EXPIRATION = 86400000L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", EXPIRATION);
    }

    private UserDetails buildUser(String username) {
        return User.builder()
                .username(username)
                .password("encodedPass")
                .authorities("ROLE_USER")
                .build();
    }

    @Test
    void generateToken_retornaStringNoNulo() {
        UserDetails userDetails = buildUser("testuser");

        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void generateToken_contieneTreePartes() {
        String token = jwtService.generateToken(buildUser("testuser"));

        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void extractUsername_retornaUsernameDelToken() {
        UserDetails userDetails = buildUser("admin");
        String token = jwtService.generateToken(userDetails);

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("admin");
    }

    @Test
    void isTokenValid_tokenValidoMismoUsuario_retornaTrue() {
        UserDetails userDetails = buildUser("chef");
        String token = jwtService.generateToken(userDetails);

        boolean valid = jwtService.isTokenValid(token, userDetails);

        assertThat(valid).isTrue();
    }

    @Test
    void isTokenValid_tokenDeOtroUsuario_retornaFalse() {
        UserDetails user1 = buildUser("user1");
        UserDetails user2 = buildUser("user2");
        String token = jwtService.generateToken(user1);

        boolean valid = jwtService.isTokenValid(token, user2);

        assertThat(valid).isFalse();
    }

    @Test
    void isTokenValid_tokenExpirado_lanzaExpiredJwtException() {
        ReflectionTestUtils.setField(jwtService, "expiration", -1000L);
        UserDetails userDetails = buildUser("testuser");
        String token = jwtService.generateToken(userDetails);
        // Restaurar para que la verificación no afecte otros tests
        ReflectionTestUtils.setField(jwtService, "expiration", EXPIRATION);

        assertThatThrownBy(() -> jwtService.isTokenValid(token, userDetails))
                .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
    }
}
