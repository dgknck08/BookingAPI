package com.example.BookingApp.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;

/**
 * Session-based authentication için özel token sınıfı
 * Password gerektirmez, session doğrulaması yeterlidir
 */
public class SessionAuthenticationToken extends AbstractAuthenticationToken {
    
    private final Object principal;
    private final String sessionId;

    public SessionAuthenticationToken(Object principal, String sessionId, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.sessionId = sessionId;
        setAuthenticated(true); // Session doğrulanmış olduğu için true
    }

    @Override
    public Object getCredentials() {
        return sessionId; // Credentials 
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    public String getSessionId() {
        return sessionId;
    }

    @Override
    public void eraseCredentials() {
    }
}