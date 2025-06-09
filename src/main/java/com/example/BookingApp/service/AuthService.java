package com.example.BookingApp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.BookingApp.dto.auth.AuthRequest;
import com.example.BookingApp.dto.auth.AuthResponse;
import com.example.BookingApp.dto.auth.RegisterRequest;
import com.example.BookingApp.dto.user.UserDto;
import com.example.BookingApp.entity.User;
import com.example.BookingApp.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

import java.util.Optional;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private SessionService sessionService;
    
    public AuthResponse login(AuthRequest request, HttpSession session) {
        try {
            Optional<User> userOpt = userRepository.findByUsername(request.getUsername());

            if (userOpt.isEmpty()) {
                return new AuthResponse("Invalid username or password", false);
            }

            User user = userOpt.get();

            if (!user.isActive()) {
                return new AuthResponse("Account is deactivated", false);
            }

            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                return new AuthResponse("Invalid username or password", false);
            }

            String sessionId = session.getId();
            sessionService.createUserSession(sessionId, user);

            return new AuthResponse("Login successful", sessionId, true);

        } catch (Exception e) {
            return new AuthResponse("Login failed: " + e.getMessage(), false);
        }
    }

    public AuthResponse register(RegisterRequest request) {
        try {
            if (userRepository.existsByUsername(request.getUsername())) {
                return new AuthResponse("Username already exists", false);
            }
            userService.createUser(request);  // Kullanıcı yaratma işini UserService’e bıraktık

            return new AuthResponse("Registration successful", true);

        } catch (Exception e) {
            return new AuthResponse("Registration failed: " + e.getMessage(), false);
        }
    }

    
    public void logout(HttpSession session) {
        String sessionId = session.getId();
        sessionService.invalidateSession(sessionId);
        session.invalidate();
    }
    
    public UserDto getCurrentUser(HttpSession session) {
        String sessionId = session.getId();
        return sessionService.getUserFromSession(sessionId);
    }
}
