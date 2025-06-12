package com.example.BookingApp.service;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.BookingApp.dto.auth.RegisterRequest;
import com.example.BookingApp.dto.user.UserDto;
import com.example.BookingApp.entity.User;
import com.example.BookingApp.entityenums.Role;
import com.example.BookingApp.mapper.UserMapper;
import com.example.BookingApp.repository.UserRepository;

import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public Optional<UserDto> getUserProfile(Long userId) {
        return userRepository.findById(userId)
                             .map(UserMapper::toDto);
    }
    
    public UserDto createUser(RegisterRequest registerRequest) {
    	User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(Role.USER);
        user.setActive(true);

        user = userRepository.save(user);
    	
        return UserMapper.toDto(user);
        
        
        
        
    }
    
    
    public UserDto updateUserProfile(Long userId, UserDto userDto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        
        user = userRepository.save(user);
        return UserMapper.toDto(user);
    }
    
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setActive(false);
        userRepository.save(user);
    }
}
