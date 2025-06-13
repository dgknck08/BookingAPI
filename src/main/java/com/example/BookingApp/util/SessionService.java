package com.example.BookingApp.util;

import com.example.BookingApp.dto.user.UserDto;
import com.example.BookingApp.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class SessionService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final Logger logger = LoggerFactory.getLogger(SessionService.class);

    private static final String SESSION_PREFIX = "session:";
    private static final String USER_SESSION_PREFIX = "user_session:";
    private static final long SESSION_TIMEOUT = 30; // 30 minutes

    public SessionService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Creates a new user session, invalidating any existing session for the user
     */
    public void createUserSession(String sessionId, User user) {
        if (sessionId == null || sessionId.isEmpty() || user == null) {
            logger.warn("Invalid parameters for createUserSession");
            return;
        }

        try {
            String sessionKey = SESSION_PREFIX + sessionId;
            String userSessionKey = USER_SESSION_PREFIX + user.getId();

            // Mevcut session'ları geçersiz kıl 
            invalidateUserSessions(user.getId());

            UserDto userDto = convertToDto(user);

            // Transaction benzeri işlem 
            redisTemplate.opsForValue().set(sessionKey, userDto, SESSION_TIMEOUT, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(userSessionKey, sessionId, SESSION_TIMEOUT, TimeUnit.MINUTES);
            
            logger.debug("Created session for user: {} with sessionId: {}", user.getUsername(), sessionId);
            
        } catch (Exception e) {
            logger.error("Error creating user session", e);
            throw new RuntimeException("Failed to create user session", e);
        }
    }

    /**
     * Retrieves user from session without extending the session
     */
    public UserDto getUserFromSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return null;
        }

        try {
            String sessionKey = SESSION_PREFIX + sessionId;
            Object userData = redisTemplate.opsForValue().get(sessionKey);
            
            if (userData instanceof UserDto) {
                UserDto userDto = (UserDto) userData;
                // Kullanıcının hala aktif 
                if (userDto.isActive()) {
                    return userDto;
                } else {
                    // Deaktif kullanıcının session'ını temizleme
                    invalidateSession(sessionId);
                    return null;
                }
            }
            
        } catch (Exception e) {
            logger.error("Error retrieving user from session: {}", sessionId, e);
        }
        
        return null;
    }

    /**
     * Extends the session timeout for both session and user session keys
     */
    public void extendSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return;
        }

        try {
            String sessionKey = SESSION_PREFIX + sessionId;
            
            if (Boolean.TRUE.equals(redisTemplate.hasKey(sessionKey))) {
                UserDto userDto = getUserFromSession(sessionId);
                
                if (userDto != null) {
                    String userSessionKey = USER_SESSION_PREFIX + userDto.getId();
                    
                    redisTemplate.expire(sessionKey, SESSION_TIMEOUT, TimeUnit.MINUTES);
                    redisTemplate.expire(userSessionKey, SESSION_TIMEOUT, TimeUnit.MINUTES);
                    
                    logger.debug("Extended session: {}", sessionId);
                }
            }
        } catch (Exception e) {
            logger.error("Error extending session: {}", sessionId, e);
        }
    }

    /**
     * Invalidates a specific session
     */
    public void invalidateSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return;
        }

        try {
            String sessionKey = SESSION_PREFIX + sessionId;
            UserDto userDto = getUserFromSession(sessionId);

            // Önce session key'ini sil
            redisTemplate.delete(sessionKey);

            // Kullanıcı varsa user session key'ini de sil
            if (userDto != null) {
                String userSessionKey = USER_SESSION_PREFIX + userDto.getId();
                redisTemplate.delete(userSessionKey);
                logger.debug("Invalidated session for user: {}", userDto.getUsername());
            }
            
        } catch (Exception e) {
            logger.error("Error invalidating session: {}", sessionId, e);
        }
    }

    /**
     * Invalidates all sessions for a specific user
     */
    public void invalidateUserSessions(Long userId) {
        if (userId == null) {
            return;
        }

        try {
            String userSessionKey = USER_SESSION_PREFIX + userId;
            String sessionId = (String) redisTemplate.opsForValue().get(userSessionKey);

            if (sessionId != null) {
                String sessionKey = SESSION_PREFIX + sessionId;
                redisTemplate.delete(sessionKey);
                redisTemplate.delete(userSessionKey);
                logger.debug("Invalidated all sessions for user: {}", userId);
            }
        } catch (Exception e) {
            logger.error("Error invalidating user sessions for userId: {}", userId, e);
        }
    }

    /**
     * Checks if a session is valid and active
     */
    public boolean isSessionValid(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return false;
        }
        
        try {
            String sessionKey = SESSION_PREFIX + sessionId;
            return Boolean.TRUE.equals(redisTemplate.hasKey(sessionKey));
        } catch (Exception e) {
            logger.error("Error checking session validity: {}", sessionId, e);
            return false;
        }
    }

    /**
     * Gets the current session ID for a user (if any)
     */
    public String getActiveSessionForUser(Long userId) {
        if (userId == null) {
            return null;
        }
        
        try {
            String userSessionKey = USER_SESSION_PREFIX + userId;
            return (String) redisTemplate.opsForValue().get(userSessionKey);
        } catch (Exception e) {
            logger.error("Error getting active session for user: {}", userId, e);
            return null;
        }
    }

    /**
     * Updates user data in the current session
     */
    public void updateUserInSession(String sessionId, User user) {
        if (sessionId == null || sessionId.isEmpty() || user == null) {
            return;
        }

        try {
            String sessionKey = SESSION_PREFIX + sessionId;
            
            if (Boolean.TRUE.equals(redisTemplate.hasKey(sessionKey))) {
                UserDto userDto = convertToDto(user);
                
                Long ttl = redisTemplate.getExpire(sessionKey, TimeUnit.SECONDS);
                if (ttl != null && ttl > 0) {
                    redisTemplate.opsForValue().set(sessionKey, userDto, ttl, TimeUnit.SECONDS);
                } else {
                    redisTemplate.opsForValue().set(sessionKey, userDto, SESSION_TIMEOUT, TimeUnit.MINUTES);
                }
                
                logger.debug("Updated user data in session: {}", sessionId);
            }
        } catch (Exception e) {
            logger.error("Error updating user in session: {}", sessionId, e);
        }
    }

    /**
     * Converts User entity to UserDto for session storage
     */
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        return dto;
    }

    /**
     * Cleanup expired sessions (can be called by scheduled task)
     */
    public void cleanupExpiredSessions() {
        // Bu method'u @Scheduled annotasyonu ile düzenli olarak çalıştırabilirsiniz
        logger.info("Session cleanup completed");
    }

    public long getSessionTimeout() {
        return SESSION_TIMEOUT;
    }
}