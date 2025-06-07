package com.example.BookingApp.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/login")
    public String loginPage() {
        return "Please login from form.";
    }

    @GetMapping("/logout-success")
    public String logoutPage() {
        return "You've been logged out.";
    }

    @GetMapping("/me")
    public String currentUser(Authentication authentication) {
        if (authentication != null) {
            return "Current user: " + authentication.getName();
        }
        return "No user logged in.";
    }
}
