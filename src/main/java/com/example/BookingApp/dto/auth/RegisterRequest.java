package com.example.BookingApp.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record RegisterRequest(
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    String username,
    
    @NotBlank(message = "Password is required")
    @Size(min = 6)
    String password,
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,
    
    @NotBlank(message = "Full name is required")
    String fullName
) {
    public RegisterRequest {
        if (username != null) {
            username = username.trim().toLowerCase();
        }
        if (email != null) {
            email = email.trim().toLowerCase();
        }
        if (fullName != null) {
            fullName = fullName.trim();
        }
        if (password != null) {
            password = password.trim();
        }
    }
}