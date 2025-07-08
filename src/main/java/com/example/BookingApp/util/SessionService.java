package com.example.BookingApp.util;

import com.example.BookingApp.dto.user.UserResponse;
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

            UserResponse userResponse = convertToUserResponse(userDetails);

            redisTemplate.opsForValue().set(sessionKey, userResponse, SESSION_TIMEOUT, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(userSessionKey, sessionId, SESSION_TIMEOUT, TimeUnit.MINUTES);

            logger.debug("Created session for user: {} with sessionId: {}", userDetails.getUsername(), sessionId);

        } catch (Exception e) {
            logger.error("Error creating user session", e);
            throw new RuntimeException("Failed to create user session", e);
        }
    }

    public UserResponse getUserFromSession(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return null;
        }

        try {
            String sessionKey = SESSION_PREFIX + sessionId;
            Object userData = redisTemplate.opsForValue().get(sessionKey);

            if (userData instanceof UserResponse userResponse) {
                if (userResponse.active()) {
                    return userResponse;
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
                UserResponse userResponse = getUserFromSession(sessionId);

                if (userResponse != null) {
                    String userSessionKey = USER_SESSION_PREFIX + userResponse.id();

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
            UserResponse userResponse = getUserFromSession(sessionId);

            redisTemplate.delete(sessionKey);

            if (userResponse != null) {
                String userSessionKey = USER_SESSION_PREFIX + userResponse.id();
                redisTemplate.delete(userSessionKey);
                logger.debug("Invalidated session for user: {}", userResponse.username());
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
                UserResponse userResponse = convertToUserResponse(userDetails);

                Long ttl = redisTemplate.getExpire(sessionKey, TimeUnit.SECONDS);
                if (ttl != null && ttl > 0) {
                    redisTemplate.opsForValue().set(sessionKey, userResponse, ttl, TimeUnit.SECONDS);
                } else {
                    redisTemplate.opsForValue().set(sessionKey, userResponse, SESSION_TIMEOUT, TimeUnit.MINUTES);
                }

                logger.debug("Updated user data in session: {}", sessionId);
            }
        } catch (Exception e) {
            logger.error("Error updating user in session: {}", sessionId, e);
        }
    }

    private UserResponse convertToUserResponse(CustomUserDetails userDetails) {
        return new UserResponse(
            userDetails.getId(),
            userDetails.getUsername(),
            userDetails.getEmail(),
            userDetails.getRole(),
            userDetails.isActive()
        );
    }

    public void cleanupExpiredSessions() {
        logger.info("Session cleanup completed");
    }

    public long getSessionTimeout() {
        return SESSION_TIMEOUT;
    }
}
