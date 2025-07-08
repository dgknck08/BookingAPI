package com.example.BookingApp.controller;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.BookingApp.dto.auth.ChangePasswordRequest;
import com.example.BookingApp.dto.user.UserResponse;
import com.example.BookingApp.dto.user.UserUpdateDto;
import com.example.BookingApp.service.AuthService;
import com.example.BookingApp.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProfileController {
    
    private final UserService userService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<UserResponse> getCurrentUserProfile(HttpSession session) {
        UserResponse currentUser = authService.getCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(currentUser);
    }
    
    @PutMapping
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UserUpdateDto userDto, HttpSession session) {
        UserResponse currentUser = authService.getCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            UserResponse updatedUser = userService.updateUser(currentUser.id(), userDto);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request, HttpSession session) {
        UserResponse currentUser = authService.getCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            userService.changePassword(currentUser.id(), request.getCurrentPassword(), request.getNewPassword());
            return ResponseEntity.ok("Password changed successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/deactivate")
    public ResponseEntity<String> deactivateAccount(HttpSession session) {
        UserResponse currentUser = authService.getCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            userService.deactivateUser(currentUser.id());
            authService.logout(session);
            return ResponseEntity.ok("Account deactivated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    
}
