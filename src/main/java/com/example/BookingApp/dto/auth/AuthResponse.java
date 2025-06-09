package com.example.BookingApp.dto.auth;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {
    private String message;
    private String sessionId;
    private boolean success;

    // Constructors
    public AuthResponse() {}

    public AuthResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    public AuthResponse(String message, String sessionId, boolean success) {
        this.message = message;
        this.sessionId = sessionId;
        this.success = success;
    }
}
