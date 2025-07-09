package com.example.BookingApp.security;

import com.example.BookingApp.dto.user.UserResponse;

public class CustomUserDetailsConverter {

    public static CustomUserDetails fromDto(UserResponse dto) {
        return new CustomUserDetails(
            dto.id(),
            dto.username(),
            dto.email(),
            null, 
            dto.role(),
            dto.active()
        );
    }
    
    public static CustomUserDetails fromUserEntity(com.example.BookingApp.entity.User user) {
        return new CustomUserDetails(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getPassword(),
            user.getRole(),
            user.isActive()
        );
    }
}