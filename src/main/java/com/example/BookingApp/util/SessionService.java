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

    public void createUserSession(String sessionId, User user) {
        String sessionKey = SESSION_PREFIX + sessionId;
        String userSessionKey = USER_SESSION_PREFIX + user.getId(); // Use ID instead of username

        UserDto userDto = convertToDto(user);

        redisTemplate.opsForValue().set(sessionKey, userDto, SESSION_TIMEOUT, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(userSessionKey, sessionId, SESSION_TIMEOUT, TimeUnit.MINUTES);
    }

    public UserDto getUserFromSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) return null;
        
        UserDto userDto = (UserDto) redisTemplate.opsForValue().get(SESSION_PREFIX + sessionId);
        
        // Extend session if user found
        if (userDto != null) {
            extendSession(sessionId, userDto);
        }
        
        return userDto;
    }

    public void extendSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) return;
        
        UserDto userDto = (UserDto) redisTemplate.opsForValue().get(SESSION_PREFIX + sessionId);
        if (userDto != null) {
            extendSession(sessionId, userDto);
        }
    }

    private void extendSession(String sessionId, UserDto userDto) {
        String sessionKey = SESSION_PREFIX + sessionId;
        String userSessionKey = USER_SESSION_PREFIX + userDto.getId();
        
        redisTemplate.expire(sessionKey, SESSION_TIMEOUT, TimeUnit.MINUTES);
        redisTemplate.expire(userSessionKey, SESSION_TIMEOUT, TimeUnit.MINUTES);
    }

    public void invalidateSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) return;
        
        String sessionKey = SESSION_PREFIX + sessionId;
        UserDto userDto = (UserDto) redisTemplate.opsForValue().get(sessionKey);

        if (userDto != null) {
            redisTemplate.delete(USER_SESSION_PREFIX + userDto.getId());
        }

        redisTemplate.delete(sessionKey);
    }

    public void invalidateUserSessions(Long userId) {
        if (userId == null) return;
        
        String userSessionKey = USER_SESSION_PREFIX + userId;
        String sessionId = (String) redisTemplate.opsForValue().get(userSessionKey);

        if (sessionId != null) {
            invalidateSession(sessionId);
        }
    }

    public boolean isSessionValid(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) return false;
        return redisTemplate.hasKey(SESSION_PREFIX + sessionId);
    }

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId()); // Include ID
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
