package com.example.BookingApp.controller;

import com.example.BookingApp.dto.auth.AuthRequest;
import com.example.BookingApp.dto.auth.AuthResponse;
import com.example.BookingApp.dto.auth.RegisterRequest;
import com.example.BookingApp.dto.user.UserResponse;
import com.example.BookingApp.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") 
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request, HttpSession session) {
        AuthResponse response = authService.login(request, session);
        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        if (response.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        authService.logout(session);
        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(HttpSession session) {
        UserResponse user = authService.getCurrentUser(session);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(401).build();
        }
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkAuth(HttpSession session) {
        boolean isAuthenticated = authService.getCurrentUser(session) != null;
        return ResponseEntity.ok(isAuthenticated);
    }
}
