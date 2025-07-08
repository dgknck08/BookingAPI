package com.example.BookingApp.dto.auth;

import com.example.BookingApp.dto.user.UserResponse;

public record AuthResponse(
    boolean success,
    String message,
    UserResponse user
) {
    public AuthResponse(boolean success, String message) {
        this(success, message, null);
    }
    
    public static AuthResponse success(String message, UserResponse user) {
        return new AuthResponse(true, message, user);
    }
   
    public static AuthResponse failure(String message) {
        return new AuthResponse(false, message, null);
    }
}