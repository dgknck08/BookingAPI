package com.example.BookingApp.util;

import com.example.BookingApp.dto.user.UserDto;
import com.example.BookingApp.entity.User;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class SessionService {

    private final RedisTemplate<String, Object> redisTemplate;

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
            return;
        }

        String sessionKey = SESSION_PREFIX + sessionId;
        String userSessionKey = USER_SESSION_PREFIX + user.getId();

        // Mevcut session'ları geçersiz kıl
        invalidateUserSessions(user.getId());

        UserDto userDto = convertToDto(user);

        // Her iki key'i de expiration ile set et
        redisTemplate.opsForValue().set(sessionKey, userDto, SESSION_TIMEOUT, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(userSessionKey, sessionId, SESSION_TIMEOUT, TimeUnit.MINUTES);
    }

    /**
     * Retrieves user from session without extending the session
     */
    public UserDto getUserFromSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return null;
        }

        String sessionKey = SESSION_PREFIX + sessionId;
        Object userData = redisTemplate.opsForValue().get(sessionKey);
        
        if (userData instanceof UserDto) {
            return (UserDto) userData;
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

        String sessionKey = SESSION_PREFIX + sessionId;
        UserDto userDto = getUserFromSession(sessionId);
        
        if (userDto != null) {
            String userSessionKey = USER_SESSION_PREFIX + userDto.getId();
            
            // Her iki key'i de atomik olarak uzat
            redisTemplate.expire(sessionKey, SESSION_TIMEOUT, TimeUnit.MINUTES);
            redisTemplate.expire(userSessionKey, SESSION_TIMEOUT, TimeUnit.MINUTES);
        }
    }

    /**
     * Invalidates a specific session
     */
    public void invalidateSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return;
        }

        String sessionKey = SESSION_PREFIX + sessionId;
        UserDto userDto = getUserFromSession(sessionId);

        // Önce session key'ini sil
        redisTemplate.delete(sessionKey);

        // Kullanıcı varsa user session key'ini de sil
        if (userDto != null) {
            String userSessionKey = USER_SESSION_PREFIX + userDto.getId();
            redisTemplate.delete(userSessionKey);
        }
    }

    /**
     * Invalidates all sessions for a specific user
     */
    public void invalidateUserSessions(Long userId) {
        if (userId == null) {
            return;
        }

        String userSessionKey = USER_SESSION_PREFIX + userId;
        String sessionId = (String) redisTemplate.opsForValue().get(userSessionKey);

        if (sessionId != null) {
            String sessionKey = SESSION_PREFIX + sessionId;
            redisTemplate.delete(sessionKey);
            redisTemplate.delete(userSessionKey);
        }
    }

    /**
     * Checks if a session is valid and active
     */
    public boolean isSessionValid(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return false;
        }
        
        String sessionKey = SESSION_PREFIX + sessionId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(sessionKey));
    }

    /**
     * Gets the current session ID for a user (if any)
     */
    public String getActiveSessionForUser(Long userId) {
        if (userId == null) {
            return null;
        }
        
        String userSessionKey = USER_SESSION_PREFIX + userId;
        return (String) redisTemplate.opsForValue().get(userSessionKey);
    }

    /**
     * Updates user data in the current session
     */
    public void updateUserInSession(String sessionId, User user) {
        if (sessionId == null || sessionId.isEmpty() || user == null) {
            return;
        }

        String sessionKey = SESSION_PREFIX + sessionId;
        
        if (Boolean.TRUE.equals(redisTemplate.hasKey(sessionKey))) {
            UserDto userDto = convertToDto(user);
            
            Long ttl = redisTemplate.getExpire(sessionKey, TimeUnit.SECONDS);
            if (ttl != null && ttl > 0) {
                redisTemplate.opsForValue().set(sessionKey, userDto, ttl, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(sessionKey, userDto, SESSION_TIMEOUT, TimeUnit.MINUTES);
            }
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

    public long getSessionTimeout() {
        return SESSION_TIMEOUT;
    }
}