package com.example.BookingApp.service;

import com.example.BookingApp.dto.auth.AuthRequest;
import com.example.BookingApp.dto.auth.AuthResponse;
import com.example.BookingApp.dto.auth.RegisterRequest;
import com.example.BookingApp.dto.user.UserResponse;
import jakarta.servlet.http.HttpSession;

public interface AuthService {
    
    AuthResponse login(AuthRequest request, HttpSession session);
    
    AuthResponse register(RegisterRequest request);
    
    void logout(HttpSession session);
    
    UserResponse getCurrentUser(HttpSession session);
}