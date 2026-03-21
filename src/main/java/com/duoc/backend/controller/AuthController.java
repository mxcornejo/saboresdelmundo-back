package com.duoc.backend.controller;

import com.duoc.backend.dto.LoginRequest;
import com.duoc.backend.dto.LoginResponse;
import com.duoc.backend.entity.User;
import com.duoc.backend.repository.UserRepository;
import com.duoc.backend.service.JwtService;
import com.duoc.backend.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserRepository userRepository;

    /**
     * POST /api/auth/login
     * API pública — devuelve un token JWT al autenticar correctamente.
     * Body: { "username": "...", "password": "..." }
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        // Valida credenciales; lanza BadCredentialsException si son incorrectas
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtService.generateToken(userDetails);

        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();

        return ResponseEntity.ok(LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .username(user.getUsername())
                .role(user.getRole())
                .build());
    }
}
