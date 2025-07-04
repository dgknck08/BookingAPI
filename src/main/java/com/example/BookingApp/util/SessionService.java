package com.example.BookingApp.util;

import com.example.BookingApp.dto.user.UserDto;
import com.example.BookingApp.security.CustomUserDetails;
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

    public void createUserSession(String sessionId, CustomUserDetails userDetails) {
        if (sessionId == null || sessionId.isEmpty() || userDetails == null) {
            logger.warn("Invalid parameters for createUserSession");
            return;
        }

        try {
            String sessionKey = SESSION_PREFIX + sessionId;
            String userSessionKey = USER_SESSION_PREFIX + userDetails.getId();
            invalidateUserSessions(userDetails.getId());

            // CustomUserDetails -> UserDto dönüşümü
            UserDto userDto = convertToDto(userDetails);

            redisTemplate.opsForValue().set(sessionKey, userDto, SESSION_TIMEOUT, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(userSessionKey, sessionId, SESSION_TIMEOUT, TimeUnit.MINUTES);

            logger.debug("Created session for user: {} with sessionId: {}", userDetails.getUsername(), sessionId);

        } catch (Exception e) {
            logger.error("Error creating user session", e);
            throw new RuntimeException("Failed to create user session", e);
        }
    }

    public UserDto getUserFromSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return null;
        }

        try {
            String sessionKey = SESSION_PREFIX + sessionId;
            Object userData = redisTemplate.opsForValue().get(sessionKey);

            if (userData instanceof UserDto) {
                UserDto userDto = (UserDto) userData;
                if (userDto.isActive()) {
                    return userDto;
                } else {
                    invalidateSession(sessionId);
                    return null;
                }
            }

        } catch (Exception e) {
            logger.error("Error retrieving user from session: {}", sessionId, e);
        }

        return null;
    }

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

    public void invalidateSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return;
        }

        try {
            String sessionKey = SESSION_PREFIX + sessionId;
            UserDto userDto = getUserFromSession(sessionId);

            redisTemplate.delete(sessionKey);

            if (userDto != null) {
                String userSessionKey = USER_SESSION_PREFIX + userDto.getId();
                redisTemplate.delete(userSessionKey);
                logger.debug("Invalidated session for user: {}", userDto.getUsername());
            }

        } catch (Exception e) {
            logger.error("Error invalidating session: {}", sessionId, e);
        }
    }

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

    public void updateUserInSession(String sessionId, CustomUserDetails userDetails) {
        if (sessionId == null || sessionId.isEmpty() || userDetails == null) {
            return;
        }

        try {
            String sessionKey = SESSION_PREFIX + sessionId;

            if (Boolean.TRUE.equals(redisTemplate.hasKey(sessionKey))) {
                UserDto userDto = convertToDto(userDetails);

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

    private UserDto convertToDto(CustomUserDetails userDetails) {
        UserDto dto = new UserDto();
        dto.setId(userDetails.getId());
        dto.setUsername(userDetails.getUsername());
        dto.setEmail(userDetails.getEmail());
        dto.setRole(userDetails.getRole());
        dto.setActive(userDetails.isActive());
        return dto;
    }

    public void cleanupExpiredSessions() {
        logger.info("Session cleanup completed");
    }

    public long getSessionTimeout() {
        return SESSION_TIMEOUT;
    }
}
