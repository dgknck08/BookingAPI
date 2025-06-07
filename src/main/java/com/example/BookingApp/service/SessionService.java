package com.example.BookingApp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


import java.util.concurrent.TimeUnit;

@Service
public class SessionService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public void storeUserSession(String sessionId, Long userId) {
        String key = "user_session:" + sessionId;
        redisTemplate.opsForValue().set(key, userId, 30, TimeUnit.MINUTES);
    }
    
    public Long getUserFromSession(String sessionId) {
        String key = "user_session:" + sessionId;
        Object userId = redisTemplate.opsForValue().get(key);
        return userId != null ? (Long) userId : null;
    }
    
    public void extendSession(String sessionId) {
        String key = "user_session:" + sessionId;
        redisTemplate.expire(key, 30, TimeUnit.MINUTES);
    }
    
    public void invalidateSession(String sessionId) {
        String key = "user_session:" + sessionId;
        redisTemplate.delete(key);
    }
    
    public void storeTempBooking(String sessionId, Long seatId, Long eventId) {
        String key = "temp_booking:" + sessionId;
        String value = eventId + ":" + seatId;
        redisTemplate.opsForValue().set(key, value, 15, TimeUnit.MINUTES);
    }
    
    public String getTempBooking(String sessionId) {
        String key = "temp_booking:" + sessionId;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? (String) value : null;
    }
    
    public void clearTempBooking(String sessionId) {
        String key = "temp_booking:" + sessionId;
        redisTemplate.delete(key);
    }
}
