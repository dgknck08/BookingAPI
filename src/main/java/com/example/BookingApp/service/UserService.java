package com.example.BookingApp.service;

import com.example.BookingApp.dto.auth.RegisterRequest;
import com.example.BookingApp.dto.user.UserResponse;
import com.example.BookingApp.dto.user.UserUpdateDto;

import java.util.Optional;

public interface UserService {
    
    UserResponse createUser(RegisterRequest request);
    
    Optional<UserResponse> getUserProfile(Long userId);
    
    UserResponse updateUser(Long userId, UserUpdateDto updateDto);
    
    void changePassword(Long userId, String currentPassword, String newPassword);
    
    void deactivateUser(Long userId);
    
    void activateUser(Long userId);
}
