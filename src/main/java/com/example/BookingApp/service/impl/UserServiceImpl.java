package com.example.BookingApp.service.impl;

import com.example.BookingApp.dto.auth.RegisterRequest;
import com.example.BookingApp.dto.user.UserResponse;
import com.example.BookingApp.dto.user.UserUpdateDto;
import com.example.BookingApp.entity.User;
import com.example.BookingApp.entityenums.Role;
import com.example.BookingApp.repository.UserRepository;
import com.example.BookingApp.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        user.setActive(true);

        User savedUser = userRepository.save(user);

        return mapToUserResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserResponse> getUserProfile(Long userId) {
        return userRepository.findById(userId)
                .map(this::mapToUserResponse);
    }

    @Override
    public UserResponse updateUser(Long userId, UserUpdateDto updateDto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (updateDto.hasEmailChange()) {
            user.setEmail(updateDto.getEmail());
        }

        if (updateDto.hasPasswordChange()) {
            if (!passwordEncoder.matches(updateDto.getCurrentPassword(), user.getPassword())) {
                throw new RuntimeException("Current password is incorrect");
            }
            user.setPassword(passwordEncoder.encode(updateDto.getNewPassword()));
        }

        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    @Override
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    public void activateUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(true);
        userRepository.save(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            user.isActive()
        );
    }
}
