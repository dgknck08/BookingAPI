package com.example.BookingApp.service;

import com.example.BookingApp.dto.auth.AuthRequest;
import com.example.BookingApp.dto.auth.AuthResponse;
import com.example.BookingApp.dto.auth.RegisterRequest;
import com.example.BookingApp.dto.user.UserDto;
import com.example.BookingApp.entity.User;
import com.example.BookingApp.repository.UserRepository;
import com.example.BookingApp.security.CustomUserDetails;
import com.example.BookingApp.security.SecurityUtils;
import com.example.BookingApp.util.SessionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final SessionService sessionService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, UserService userService,
                       SessionService sessionService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.sessionService = sessionService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse login(AuthRequest request, HttpSession session) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            
            Optional<User> userOpt = userRepository.findById(userDetails.getId());
            if (userOpt.isEmpty()) {
                return new AuthResponse("User not found", false);
            }
            
            User user = userOpt.get();
            
            if (!user.isActive()) {
                return new AuthResponse("Account is deactivated", false);
            }
            
            sessionService.createUserSession(session.getId(), user);
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            return new AuthResponse("Login successful", session.getId(), true);
            
        } catch (DisabledException e) {
            return new AuthResponse("Account is deactivated", false);
        } catch (BadCredentialsException e) {
            return new AuthResponse("Invalid username or password", false);
        } catch (AuthenticationException e) {
            return new AuthResponse("Authentication failed: " + e.getMessage(), false);
        } catch (Exception e) {
            return new AuthResponse("Login failed: " + e.getMessage(), false);
        }
    }

    public AuthResponse register(RegisterRequest request) {
        try {
            if (userRepository.existsByUsername(request.getUsername())) {
                return new AuthResponse("Username already exists", false);
            }
            
            if (userRepository.existsByEmail(request.getEmail())) {
                return new AuthResponse("Email already exists", false);
            }

            userService.createUser(request);
            return new AuthResponse("Registration successful", true);
            
        } catch (Exception e) {
            return new AuthResponse("Registration failed: " + e.getMessage(), false);
        }
    }

    public void logout(HttpSession session) {
        try {
            if (session != null) {
                sessionService.invalidateSession(session.getId());
                
                SecurityContextHolder.clearContext();
                
                session.invalidate();
            }
        } catch (Exception e) {
            System.err.println("Logout error: " + e.getMessage());
        }
    }

    public UserDto getCurrentUser(HttpSession session) {
        try {
            if (session == null) {
                return null;
            }
            
            UserDto userFromSession = sessionService.getUserFromSession(session.getId());
            if (userFromSession != null) {
                return userFromSession;
            }
            
            return SecurityUtils.getCurrentUser();
            
        } catch (Exception e) {
            return null;
        }
    }
    
    public boolean isSessionValid(HttpSession session) {
        if (session == null) {
            return false;
        }
        
        try {
            return sessionService.isSessionValid(session.getId()) && 
                   sessionService.getUserFromSession(session.getId()) != null;
        } catch (Exception e) {
            return false;
        }
    }
}