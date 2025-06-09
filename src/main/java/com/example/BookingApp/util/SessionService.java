package com.example.BookingApp.util;


import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.example.BookingApp.dto.user.UserDto;
import com.example.BookingApp.entity.User;




@Service
public class SessionService {
    
    private static final String SESSION_CACHE = "sessions";
    private static final String USER_SESSION_CACHE = "userSessions";
    
    @CachePut(value = SESSION_CACHE, key = "#sessionId")
    public UserDto createUserSession(String sessionId, User user) {
        UserDto userDto = convertToDto(user);
        
        // user-->session
        cacheUserToSession(user.getId(), sessionId);
        
        return userDto; //sesionId = key
    }
    
    @Cacheable(value = SESSION_CACHE, key = "#sessionId")
    public UserDto getUserFromSession(String sessionId) {
        // null -> cache miss
        // Spring Caching
        return null;
    }
    
    @CachePut(value = USER_SESSION_CACHE, key = "#userId")
    public String cacheUserToSession(Long userId, String sessionId) {
        return sessionId;
    }
    
    @CacheEvict(value = SESSION_CACHE, key = "#sessionId")
    public void invalidateSession(String sessionId) {
        //user-->session
        UserDto userDto = getUserFromSession(sessionId);
        if (userDto != null) {
            invalidateUserSession(userDto.getId());
        }
        //@CacheEvict
    }
    
    @CacheEvict(value = USER_SESSION_CACHE, key = "#userId")
    public void invalidateUserSession(Long userId) {
        // User session cache will be evicted automatically
    }
    
    @CacheEvict(value = {SESSION_CACHE, USER_SESSION_CACHE}, allEntries = true)
    public void invalidateAllSessions() {
        // Clears all sessions - useful for maintenance
    }
    
    public boolean isSessionValid(String sessionId) {
        return getUserFromSession(sessionId) != null;
    }
    
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setActive(user.isActive());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
