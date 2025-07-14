package com.example.BookingApp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.BookingApp.dto.auth.ChangePasswordRequest;
import com.example.BookingApp.dto.user.UserResponse;
import com.example.BookingApp.dto.user.UserUpdateDto;
import com.example.BookingApp.exception.UserNotLoggedInException;
import com.example.BookingApp.exception.ValidationException;
import com.example.BookingApp.service.AuthService;
import com.example.BookingApp.service.UserService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class ProfileController {

    private final UserService userService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<UserResponse> getCurrentUserProfile(HttpSession session) {
        log.info("Fetching current user profile - Session ID: {}", session.getId());
        
        try {
            UserResponse currentUser = authService.getCurrentUser(session);
            if (currentUser == null) {
                log.warn("Unauthorized profile access attempt - Session ID: {}", session.getId());
                throw new UserNotLoggedInException("Authentication required");
            }
            
            log.info("Successfully retrieved profile for user ID: {}", currentUser.id());
            
            return ResponseEntity.ok(currentUser);
            
        } catch (UserNotLoggedInException e) {
            log.error("Authentication error while fetching profile: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while fetching profile: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve profile: " + e.getMessage());
        }
    }

    @PutMapping
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UserUpdateDto userDto, HttpSession session) {
        log.info("Updating user profile - Session ID: {}", session.getId());
        
        try {
            UserResponse currentUser = authService.getCurrentUser(session);
            if (currentUser == null) {
                log.warn("Unauthorized profile update attempt - Session ID: {}", session.getId());
                throw new UserNotLoggedInException("Authentication required");
            }
            
            UserResponse updatedUser = userService.updateUser(currentUser.id(), userDto);
            
            log.info("Profile updated successfully for user ID: {}", currentUser.id());
            
            return ResponseEntity.ok(updatedUser);
            
        } catch (UserNotLoggedInException e) {
            log.error("Authentication error during profile update: {}", e.getMessage());
            throw e;
        } catch (ValidationException e) {
            log.error("Validation error during profile update - User ID: {}, Error: {}", 
                    getCurrentUserIdSafely(session), e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            log.error("Runtime error during profile update - User ID: {}, Error: {}", 
                    getCurrentUserIdSafely(session), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during profile update - User ID: {}, Error: {}", 
                    getCurrentUserIdSafely(session), e.getMessage(), e);
            throw new RuntimeException("Failed to update profile: " + e.getMessage());
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<SuccessResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request, HttpSession session) {
        log.info("Password change request - Session ID: {}", session.getId());
        
        try {
            UserResponse currentUser = authService.getCurrentUser(session);
            if (currentUser == null) {
                log.warn("Unauthorized password change attempt - Session ID: {}", session.getId());
                throw new UserNotLoggedInException("Authentication required");
            }
            
            if (request.getCurrentPassword() == null || request.getCurrentPassword().trim().isEmpty()) {
                log.warn("Empty current password provided for user ID: {}", currentUser.id());
                throw new ValidationException("Current password is required");
            }
            
            if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
                log.warn("Empty new password provided for user ID: {}", currentUser.id());
                throw new ValidationException("New password is required");
            }
            
            userService.changePassword(currentUser.id(), request.getCurrentPassword(), request.getNewPassword());
            
            log.info("Password changed successfully for user ID: {}", currentUser.id());
            
            return ResponseEntity.ok(new SuccessResponse("Password changed successfully"));
            
        } catch (UserNotLoggedInException e) {
            log.error("Authentication error during password change: {}", e.getMessage());
            throw e;
        } catch (ValidationException e) {
            log.error("Validation error during password change - User ID: {}, Error: {}", 
                    getCurrentUserIdSafely(session), e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            log.error("Runtime error during password change - User ID: {}, Error: {}", 
                    getCurrentUserIdSafely(session), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during password change - User ID: {}, Error: {}", 
                    getCurrentUserIdSafely(session), e.getMessage(), e);
            throw new RuntimeException("Failed to change password: " + e.getMessage());
        }
    }

    @PostMapping("/deactivate")
    public ResponseEntity<SuccessResponse> deactivateAccount(HttpSession session) {
        log.info("Account deactivation request - Session ID: {}", session.getId());
        
        try {
            UserResponse currentUser = authService.getCurrentUser(session);
            if (currentUser == null) {
                log.warn("Unauthorized account deactivation attempt - Session ID: {}", session.getId());
                throw new UserNotLoggedInException("Authentication required");
            }
            
            userService.deactivateUser(currentUser.id());
            
            log.info("Account deactivated successfully for user ID: {}", currentUser.id());
            
            authService.logout(session);
            
            return ResponseEntity.ok(new SuccessResponse("Account deactivated successfully"));
            
        } catch (UserNotLoggedInException e) {
            log.error("Authentication error during account deactivation: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            log.error("Runtime error during account deactivation - User ID: {}, Error: {}", 
                    getCurrentUserIdSafely(session), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during account deactivation - User ID: {}, Error: {}", 
                    getCurrentUserIdSafely(session), e.getMessage(), e);
            throw new RuntimeException("Failed to deactivate account: " + e.getMessage());
        }
    }
    
    private Long getCurrentUserIdSafely(HttpSession session) {
        try {
            UserResponse user = authService.getCurrentUser(session);
            return user != null ? user.id() : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    public static class SuccessResponse {
        private String message;
        
        public SuccessResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
}