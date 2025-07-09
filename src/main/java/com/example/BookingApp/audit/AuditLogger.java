package com.example.BookingApp.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class AuditLogger {
    
    public void logSuccessfulLogin(Long userId, String username, String sessionId) {
        log.info("AUDIT: Successful login - User ID: {}, Username: {}, Session: {}, Time: {}", 
                userId, username, sessionId, LocalDateTime.now());
    }
    
    public void logFailedLogin(String username, String reason, String clientInfo) {
        log.warn("AUDIT: Failed login - Username: {}, Reason: {}, Client: {}, Time: {}", 
                username, reason, clientInfo, LocalDateTime.now());
    }
    
    public void logSuccessfulRegistration(Long userId, String username) {
        log.info("AUDIT: Successful registration - User ID: {}, Username: {}, Time: {}", 
                userId, username, LocalDateTime.now());
    }
    
    public void logFailedRegistration(String username, String reason) {
        log.warn("AUDIT: Failed registration - Username: {}, Reason: {}, Time: {}", 
                username, reason, LocalDateTime.now());
    }
    
    public void logLogout(Long userId, String sessionId) {
        log.info("AUDIT: User logout - User ID: {}, Session: {}, Time: {}", 
                userId, sessionId, LocalDateTime.now());
    }
}