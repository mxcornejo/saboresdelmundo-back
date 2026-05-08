package com.duoc.backend.controller;

import com.duoc.backend.dto.LoginRequest;
import com.duoc.backend.dto.LoginResponse;
import com.duoc.backend.dto.RegisterRequest;
import com.duoc.backend.entity.User;
import com.duoc.backend.repository.UserRepository;
import com.duoc.backend.service.JwtService;
import com.duoc.backend.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * POST /api/auth/login
     * API pública — devuelve un token JWT al autenticar correctamente.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
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

    /**
     * POST /api/auth/register
     * API pública — registra un nuevo usuario con contraseña encriptada.
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()
                || request.getEmail() == null || request.getEmail().isBlank()
                || request.getFullName() == null || request.getFullName().isBlank()) {
            return ResponseEntity.badRequest().body("Todos los campos son obligatorios.");
        }
        if (request.getUsername().length() < 3 || request.getUsername().length() > 50) {
            return ResponseEntity.badRequest().body("El nombre de usuario debe tener entre 3 y 50 caracteres.");
        }
        if (request.getPassword().length() < 8) {
            return ResponseEntity.badRequest().body("La contraseña debe tener al menos 8 caracteres.");
        }
        if (!request.getEmail().contains("@")) {
            return ResponseEntity.badRequest().body("El correo electrónico no es válido.");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El nombre de usuario ya está en uso.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El correo electrónico ya está registrado.");
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setFullName(request.getFullName());
        newUser.setEmail(request.getEmail());
        newUser.setRole("ROLE_USER");
        newUser.setEnabled(true);
        userRepository.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado exitosamente.");
    }
}
