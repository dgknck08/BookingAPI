package com.example.BookingApp;



import com.example.BookingApp.dto.auth.AuthRequest;
import com.example.BookingApp.dto.auth.AuthResponse;
import com.example.BookingApp.dto.auth.RegisterRequest;
import com.example.BookingApp.dto.user.UserResponse;
import com.example.BookingApp.entity.User;
import com.example.BookingApp.entityenums.Role;
import com.example.BookingApp.exception.*;
import com.example.BookingApp.repository.UserRepository;
import com.example.BookingApp.service.UserService;
import com.example.BookingApp.service.impl.AuthServiceImpl;
import com.example.BookingApp.audit.AuditLogger;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private UserService userService;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private AuditLogger auditLogger;
    
    @Mock
    private HttpSession session;
    
    @InjectMocks
    private AuthServiceImpl authService;
    
    private User testUser;
    private UserResponse testUserResponse;
    private AuthRequest validAuthRequest;
    private RegisterRequest validRegisterRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .role(Role.USER)
                .active(true)
                .build();
                

        testUserResponse = new UserResponse(
            1L, 
            "testuser", 
            "test@example.com", 
            Role.USER, 
            true
        );

        
        validAuthRequest = new AuthRequest("testuser", "password123");
        
        validRegisterRequest = new RegisterRequest(
        	    "newuser",
        	    "Password123!",
        	    "newuser@example.com",
        	    "New User"
        	);
    }
    
    @Test
    void login_WithValidCredentials_ShouldReturnSuccessResponse() {
      
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(userService.getUserProfile(1L)).thenReturn(Optional.of(testUserResponse));
        when(session.getId()).thenReturn("session123");

        AuthResponse response = authService.login(validAuthRequest, session);

        assertTrue(response.success());
        assertEquals("Login successful", response.message());
        assertEquals(testUserResponse, response.user());
        
        verify(session).setAttribute("userId", 1L);
        verify(session).setAttribute(eq("loginTime"), any());
        verify(session).setAttribute("userRole", Role.USER); 
        verify(auditLogger).logSuccessfulLogin(1L, "testuser", "session123");
    }

    @Test
    void login_WithInvalidUsername_ShouldThrowInvalidCredentialsException() {
        
        when(userRepository.findByUsername("invaliduser")).thenReturn(Optional.empty());

        AuthRequest invalidRequest = new AuthRequest("invaliduser", "password123");

        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login(invalidRequest, session);
        });
        
        verify(auditLogger).logFailedLogin(eq("invaliduser"), any(), any());
    }

    @Test
    void login_WithInvalidPassword_ShouldThrowInvalidCredentialsException() {

    	when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", "encodedPassword")).thenReturn(false);

        AuthRequest invalidRequest = new AuthRequest("testuser", "wrongpassword");

        assertThrows(InvalidCredentialsException.class, () -> {
            authService.login(invalidRequest, session);
        });
        
        verify(auditLogger).logFailedLogin(eq("testuser"), any(), any());
    }

    @Test
    void login_WithInactiveUser_ShouldThrowUserInactiveException() {

    	User inactiveUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .active(false)
                .build();
                
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(inactiveUser));

        // When & Then
        assertThrows(UserInactiveException.class, () -> {
            authService.login(validAuthRequest, session);
        });
    }

    @Test
    void login_WithEmptyUsername_ShouldThrowValidationException() {
        AuthRequest emptyUsernameRequest = new AuthRequest("", "password123");

        assertThrows(ValidationException.class, () -> {
            authService.login(emptyUsernameRequest, session);
        });
    }

    @Test
    void login_WithEmptyPassword_ShouldThrowValidationException() {
        AuthRequest emptyPasswordRequest = new AuthRequest("testuser", "");
        assertThrows(ValidationException.class, () -> {
            authService.login(emptyPasswordRequest, session);
        });
    }

    @Test
    void register_WithValidRequest_ShouldReturnSuccessResponse() {
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(userService.createUser(validRegisterRequest)).thenReturn(testUserResponse);

        AuthResponse response = authService.register(validRegisterRequest);

        // Then
        assertTrue(response.success());
        assertEquals("Registration successful", response.message());
        assertEquals(testUserResponse, response.user());
        
        verify(auditLogger).logSuccessfulRegistration(1L, "newuser");
    }

    @Test
    void register_WithExistingUsername_ShouldThrowUserAlreadyExistsException() {
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.of(testUser));

        assertThrows(UserAlreadyExistsException.class, () -> {
            authService.register(validRegisterRequest);
        });
        
        verify(auditLogger).logFailedRegistration(eq("newuser"), any());
    }

    @Test
    void register_WithExistingEmail_ShouldThrowUserAlreadyExistsException() {
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.of(testUser));

        assertThrows(UserAlreadyExistsException.class, () -> {
            authService.register(validRegisterRequest);
        });
    }


    @Test
    void logout_WithValidSession_ShouldInvalidateSession() {
        when(session.getAttribute("userId")).thenReturn(1L);
        when(session.getId()).thenReturn("session123");

        authService.logout(session);

        verify(session).invalidate();
        verify(auditLogger).logLogout(1L, "session123");
    }

    @Test
    void logout_WithNullUserId_ShouldInvalidateSession() {
        when(session.getAttribute("userId")).thenReturn(null);

        authService.logout(session);

        verify(session).invalidate();
        verify(auditLogger, never()).logLogout(any(), any());
    }

    @Test
    void getCurrentUser_WithValidSession_ShouldReturnUserResponse() {
        when(session.getAttribute("userId")).thenReturn(1L);
        when(userService.getUserProfile(1L)).thenReturn(Optional.of(testUserResponse));

        UserResponse response = authService.getCurrentUser(session);

        assertEquals(testUserResponse, response);
    }

    @Test
    void getCurrentUser_WithNoUserInSession_ShouldThrowUserNotLoggedInException() {
        when(session.getAttribute("userId")).thenReturn(null);

        assertThrows(UserNotLoggedInException.class, () -> {
            authService.getCurrentUser(session);
        });
    }

    @Test
    void getCurrentUser_WithNonExistentUser_ShouldThrowUserProfileNotFoundException() {
        // Given
        when(session.getAttribute("userId")).thenReturn(1L);
        when(userService.getUserProfile(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserProfileNotFoundException.class, () -> {
            authService.getCurrentUser(session);
        });
    }

    @Test
    void isUserLoggedIn_WithValidSession_ShouldReturnTrue() {
        when(session.getAttribute("userId")).thenReturn(1L);

        boolean result = authService.isUserLoggedIn(session);

        assertTrue(result);
    }

    @Test
    void isUserLoggedIn_WithNoUserInSession_ShouldReturnFalse() {
        when(session.getAttribute("userId")).thenReturn(null);

        boolean result = authService.isUserLoggedIn(session);

       
        assertFalse(result);
    }

    @Test
    void isUserLoggedIn_WithException_ShouldReturnFalse() {
        when(session.getAttribute("userId")).thenThrow(new RuntimeException("Session error"));

        boolean result = authService.isUserLoggedIn(session);


        assertFalse(result);
    }
}