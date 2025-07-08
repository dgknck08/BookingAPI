package com.example.BookingApp.mapper;

import com.example.BookingApp.dto.auth.RegisterRequest;
import com.example.BookingApp.dto.user.UserResponse;
import com.example.BookingApp.entity.User;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    UserResponse toResponse(User user);
    
    List<UserResponse> toResponseList(List<User> users);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) 
    @Mapping(target = "role", ignore = true) 
    @Mapping(target = "active", ignore = true) 
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(RegisterRequest request);
}
