package com.example.BookingApp.mapper;

import com.example.BookingApp.dto.user.UserDto;
import com.example.BookingApp.entity.User;

public class UserMapper {

    public static UserDto toDto(User user) {
        if (user == null) return null;

        UserDto dto = new UserDto();
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        return dto;
    }

    public static User toEntity(UserDto dto) {
        if (dto == null) return null;

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setActive(dto.isActive());
        return user;
    }
    
    
}
