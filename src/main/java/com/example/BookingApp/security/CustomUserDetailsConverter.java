package com.example.BookingApp.security;

import com.example.BookingApp.dto.user.UserDto;

public class CustomUserDetailsConverter {

    public static CustomUserDetails fromDto(UserDto dto) {
        return new CustomUserDetails(
                dto.getId(),
                dto.getUsername(),
                dto.getEmail(),
                null, // Redis'te password saklamıyorsun, gerek yok
                dto.getRole(),
                dto.isActive()
        );
    }
}
