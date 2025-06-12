package com.example.BookingApp.security;

import com.example.BookingApp.dto.user.UserDto;
import com.example.BookingApp.entityenums.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {
    
    /**
     * Get currently authenticated user as CustomUserDetails
     */
    public static CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            return (CustomUserDetails) authentication.getPrincipal();
        }
        return null;
    }
    
    /**
     * Get currently authenticated user as UserDto (converted from CustomUserDetails)
     */
    public static UserDto getCurrentUser() {
        CustomUserDetails userDetails = getCurrentUserDetails();
        if (userDetails != null) {
            return convertToUserDto(userDetails);
        }
        return null;
    }
    
    /**
     * Check if current user has specific role
     */
    public static boolean hasRole(Role role) {
        CustomUserDetails userDetails = getCurrentUserDetails();
        return userDetails != null && userDetails.getRole() == role;
    }
    
    /**
     * Check if current user is admin
     */
    public static boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }
    
    /**
     * Check if current user is the owner of the resource
     */
    public static boolean isOwner(Long userId) {
        CustomUserDetails userDetails = getCurrentUserDetails();
        return userDetails != null && userDetails.getId().equals(userId);
    }
    
    /**
     * Check if current user is admin or owner of the resource
     */
    public static boolean isAdminOrOwner(Long userId) {
        return isAdmin() || isOwner(userId);
    }
    
    /**
     * Get current user ID
     */
    public static Long getCurrentUserId() {
        CustomUserDetails userDetails = getCurrentUserDetails();
        return userDetails != null ? userDetails.getId() : null;
    }
    
    /**
     * Check if user is authenticated
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && 
               authentication.isAuthenticated() && 
               authentication.getPrincipal() instanceof CustomUserDetails;
    }
    
    /**
     * Get current username
     */
    public static String getCurrentUsername() {
        CustomUserDetails userDetails = getCurrentUserDetails();
        return userDetails != null ? userDetails.getUsername() : null;
    }
    
    /**
     * Convert CustomUserDetails to UserDto
     */
    private static UserDto convertToUserDto(CustomUserDetails userDetails) {
        UserDto dto = new UserDto();
        dto.setId(userDetails.getId());
        dto.setUsername(userDetails.getUsername());
        dto.setEmail(userDetails.getEmail());
        dto.setRole(userDetails.getRole());
        dto.setActive(userDetails.isActive());
        return dto;
    }
}
