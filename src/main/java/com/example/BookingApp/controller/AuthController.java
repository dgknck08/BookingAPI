package com.example.BookingApp.controller;

import com.example.BookingApp.dto.auth.AuthRequest;
import com.example.BookingApp.dto.auth.AuthResponse;
import com.example.BookingApp.dto.auth.RegisterRequest;
import com.example.BookingApp.dto.user.UserDto;
import com.example.BookingApp.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request, HttpSession session) {
        AuthResponse response = authService.login(request, session);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return response.isSuccess() ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        authService.logout(session);
        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(HttpSession session) {
        UserDto user = authService.getCurrentUser(session);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.status(401).build();
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkAuth(HttpSession session) {
        return ResponseEntity.ok(authService.getCurrentUser(session) != null);
    }
}
