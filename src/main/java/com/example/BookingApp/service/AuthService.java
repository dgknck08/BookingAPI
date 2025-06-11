package com.example.BookingApp.service;

import com.example.BookingApp.dto.auth.AuthRequest;
import com.example.BookingApp.dto.auth.AuthResponse;
import com.example.BookingApp.dto.auth.RegisterRequest;
import com.example.BookingApp.dto.user.UserDto;
import com.example.BookingApp.entity.User;
import com.example.BookingApp.repository.UserRepository;
import com.example.BookingApp.util.SessionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;

    public AuthService(UserRepository userRepository, UserService userService,
                       PasswordEncoder passwordEncoder, SessionService sessionService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.sessionService = sessionService;
    }

    public AuthResponse login(AuthRequest request, HttpSession session) {
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

        
        //redise taşıyorum sessionı
        sessionService.createUserSession(session.getId(), user);
        return new AuthResponse("Login successful", session.getId(), true);
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return new AuthResponse("Username already exists", false);
        }

        try {
            userService.createUser(request);
            return new AuthResponse("Registration successful", true);
        } catch (Exception e) {
            return new AuthResponse("Registration failed: " + e.getMessage(), false);
        }
    }

    public void logout(HttpSession session) {
        sessionService.invalidateSession(session.getId());
        session.invalidate();
    }

    public UserDto getCurrentUser(HttpSession session) {
        return sessionService.getUserFromSession(session.getId());
    }
}
