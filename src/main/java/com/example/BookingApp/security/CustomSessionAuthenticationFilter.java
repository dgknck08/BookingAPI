package com.example.BookingApp.security;

import com.example.BookingApp.dto.user.UserResponse;
import com.example.BookingApp.util.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class CustomSessionAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(CustomSessionAuthenticationFilter.class);
    private final SessionService sessionService;

    public CustomSessionAuthenticationFilter(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        try {
            Authentication existingAuth = SecurityContextHolder.getContext().getAuthentication();
            if (existingAuth != null && existingAuth.isAuthenticated() &&
                !(existingAuth.getPrincipal() instanceof String && "anonymousUser".equals(existingAuth.getPrincipal()))) {
                filterChain.doFilter(request, response);
                return;
            }

            String requestPath = request.getRequestURI();
            if (isPublicEndpoint(requestPath)) {
                filterChain.doFilter(request, response);
                return;
            }

            HttpSession session = request.getSession(false);
            if (session != null) {
                String sessionId = session.getId();
                UserResponse userResponse = sessionService.getUserFromSession(sessionId);

                if (userResponse != null && userResponse.active()) {
                    CustomUserDetails userDetails = CustomUserDetailsConverter.fromDto(userResponse);

                    SessionAuthenticationToken authToken = new SessionAuthenticationToken(
                            userDetails,
                            sessionId,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    sessionService.extendSession(sessionId);
                } else {
                    SecurityContextHolder.clearContext();
                }
            }
        } catch (Exception e) {
            logger.error("Session authentication failed", e);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/auth/")
                || path.startsWith("/api/events/public/")
                || path.startsWith("/actuator/")
                || path.equals("/error")
                || path.startsWith("/static/")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/");
    }
}
