package com.example.BookingApp.service.impl;

import com.example.BookingApp.dto.auth.AuthRequest;
import com.example.BookingApp.dto.auth.AuthResponse;
import com.example.BookingApp.dto.auth.RegisterRequest;
import com.example.BookingApp.dto.user.UserResponse;
import com.example.BookingApp.entity.User;
import com.example.BookingApp.exception.*;
import com.example.BookingApp.repository.UserRepository;
import com.example.BookingApp.service.AuthService;
import com.example.BookingApp.service.UserService;
import com.example.BookingApp.util.ValidationUtils;
import com.example.BookingApp.audit.AuditLogger;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogger auditLogger;
    
    private static final String USER_ID_SESSION_KEY = "userId";
    private static final String LOGIN_TIME_SESSION_KEY = "loginTime";
    private static final String USER_ROLE_SESSION_KEY = "userRole";

    @Override
    public AuthResponse login(AuthRequest request, HttpSession session) {
        log.info("Login attempt initiated for username: {}", request.username());
        
        try {
            validateLoginRequest(request);
            
            User user = findUserByUsername(request.username());
            
            validateUserState(user);
            
            validatePassword(request.password(), user.getPassword(), request.username());
            
            createUserSession(session, user);
            
            UserResponse userResponse = getUserProfile(user.getId());
            
            auditLogger.logSuccessfulLogin(user.getId(), user.getUsername(), session.getId());
            log.info("Login successful for user: {} (ID: {})", request.username(), user.getId());
            
            return AuthResponse.success("Login successful", userResponse);
            
        } catch (Exception e) {
            auditLogger.logFailedLogin(request.username(), e.getMessage(), getClientInfo(session));
            log.error("Login failed for username: {} - Error: {}", request.username(), e.getMessage());
            throw e;
        }
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        log.info("Registration attempt initiated for username: {}", request.username());
        
        try {
            validateRegistrationRequest(request);
            
            checkUserExistence(request);
            
            UserResponse userResponse = userService.createUser(request);
            
            auditLogger.logSuccessfulRegistration(userResponse.id(), request.username());
            log.info("User registered successfully: {} (ID: {})", request.username(), userResponse.id());
            
            return AuthResponse.success("Registration successful", userResponse);
            
        } catch (Exception e) {
            auditLogger.logFailedRegistration(request.username(), e.getMessage());
            log.error("Registration failed for username: {} - Error: {}", request.username(), e.getMessage());
            throw e;
        }
    }

    @Override
    public void logout(HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute(USER_ID_SESSION_KEY);
            String sessionId = session.getId();
            
            if (userId != null) {
                auditLogger.logLogout(userId, sessionId);
                log.info("User logged out successfully - User ID: {}, Session ID: {}", userId, sessionId);
            }
            
            session.invalidate();
            
        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
            throw new AuthenticationException("Logout failed: " + e.getMessage());
        }
    }

    @Override
    public UserResponse getCurrentUser(HttpSession session) {
        try {
            Long userId = getCurrentUserId(session);
            return getUserProfile(userId);
        } catch (Exception e) {
            log.error("Error retrieving current user: {}", e.getMessage());
            throw e;
        }
    }


    @Override
    public boolean isUserLoggedIn(HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute(USER_ID_SESSION_KEY);
            return userId != null;
        } catch (Exception e) {
            log.debug("Error checking login status: {}", e.getMessage());
            return false;
        }
    }

    private void validateLoginRequest(AuthRequest request) {
        if (request == null) {
            throw new ValidationException("Login request cannot be null");
        }
        
        if (!StringUtils.hasText(request.username())) {
            throw new ValidationException("Username is required");
        }
        
        if (!StringUtils.hasText(request.password())) {
            throw new ValidationException("Password is required");
        }
        
        if (!ValidationUtils.isValidUsername(request.username())) {
            throw new ValidationException("Invalid username format");
        }
    }

    private void validateRegistrationRequest(RegisterRequest request) {
        if (request == null) {
            throw new ValidationException("Registration request cannot be null");
        }
        
        if (!StringUtils.hasText(request.username())) {
            throw new ValidationException("Username is required");
        }
        
        if (!StringUtils.hasText(request.email())) {
            throw new ValidationException("Email is required");
        }
        
        if (!StringUtils.hasText(request.password())) {
            throw new ValidationException("Password is required");
        }
        
        if (!ValidationUtils.isValidUsername(request.username())) {
            throw new ValidationException("Invalid username format");
        }
        
        if (!ValidationUtils.isValidEmail(request.email())) {
            throw new ValidationException("Invalid email format");
        }
        
        if (!ValidationUtils.isValidPassword(request.password())) {
            throw new ValidationException("Password does not meet requirements");
        }
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found with username: {}", username);
                    return new InvalidCredentialsException("Invalid credentials");
                });
    }

    private void validateUserState(User user) {
        if (!user.isActive()) {
            log.warn("Inactive account login attempt for user ID: {}", user.getId());
            throw new UserInactiveException("Account is deactivated");
        }
    }

    private void validatePassword(String rawPassword, String encodedPassword, String username) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            log.warn("Invalid password attempt for username: {}", username);
            throw new InvalidCredentialsException("Invalid credentials");
        }
    }

    private void createUserSession(HttpSession session, User user) {
        session.setAttribute(USER_ID_SESSION_KEY, user.getId());
        session.setAttribute(LOGIN_TIME_SESSION_KEY, LocalDateTime.now());
        session.setAttribute(USER_ROLE_SESSION_KEY, user.getRole());
        
        log.info("Session created for user ID: {} with session ID: {}", user.getId(), session.getId());
    }

    private UserResponse getUserProfile(Long userId) {
        return userService.getUserProfile(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("User profile not found"));
    }

    private Long getCurrentUserId(HttpSession session) {
        Long userId = (Long) session.getAttribute(USER_ID_SESSION_KEY);
        
        if (userId == null) {
            log.warn("No user in session - Session ID: {}", session.getId());
            throw new UserNotLoggedInException("User not logged in");
        }
        
        return userId;
    }

    private void checkUserExistence(RegisterRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            log.warn("Username already exists: {}", request.username());
            throw new UserAlreadyExistsException("Username already exists");
        }

        if (userRepository.findByEmail(request.email()).isPresent()) {
            log.warn("Email already exists: {}", request.email());
            throw new UserAlreadyExistsException("Email already exists");
        }
    }

    private String getClientInfo(HttpSession session) {
        return "Session: " + session.getId();
    }
}