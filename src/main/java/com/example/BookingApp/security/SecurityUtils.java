package com.example.BookingApp.security;

import com.example.BookingApp.dto.user.UserResponse;
import com.example.BookingApp.entityenums.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public static CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return (CustomUserDetails) authentication.getPrincipal();
        }
        return null;
    }

    public static UserResponse getCurrentUser() {
        CustomUserDetails userDetails = getCurrentUserDetails();
        if (userDetails != null) {
            return new UserResponse(
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getRole(),
                userDetails.isActive()
            );
        }
        return null;
    }

    public static boolean hasRole(Role role) {
        CustomUserDetails userDetails = getCurrentUserDetails();
        return userDetails != null && userDetails.getRole() == role;
    }

    public static boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

    public static boolean isOwner(Long userId) {
        CustomUserDetails userDetails = getCurrentUserDetails();
        return userDetails != null && userDetails.getId().equals(userId);
    }

    public static boolean isAdminOrOwner(Long userId) {
        return isAdmin() || isOwner(userId);
    }

    public static Long getCurrentUserId() {
        CustomUserDetails userDetails = getCurrentUserDetails();
        return userDetails != null ? userDetails.getId() : null;
    }

    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null &&
               authentication.isAuthenticated() &&
               authentication.getPrincipal() instanceof CustomUserDetails;
    }

    public static String getCurrentUsername() {
        CustomUserDetails userDetails = getCurrentUserDetails();
        return userDetails != null ? userDetails.getUsername() : null;
    }
}
