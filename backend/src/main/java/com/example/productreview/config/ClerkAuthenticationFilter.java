package com.example.productreview.config;

import com.example.productreview.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class ClerkAuthenticationFilter extends OncePerRequestFilter {

    static final String AUTHENTICATED_CLERK_USER_ID_ATTRIBUTE = "authenticatedClerkUserId";
    static final String AUTHENTICATED_INTERNAL_USER_ID_ATTRIBUTE = "authenticatedInternalUserId";
    static final String AUTHENTICATED_USER_ID_ATTRIBUTE = AUTHENTICATED_CLERK_USER_ID_ATTRIBUTE;

    private static final Logger log = LoggerFactory.getLogger(ClerkAuthenticationFilter.class);

    private final ClerkAuthProperties authProperties;
    private final ClerkJwtVerifier tokenVerifier;
    private final ObjectMapper objectMapper;

    public ClerkAuthenticationFilter(ClerkAuthProperties authProperties,
                                     ClerkJwtVerifier tokenVerifier,
                                     ObjectMapper objectMapper) {
        this.authProperties = authProperties;
        this.tokenVerifier = tokenVerifier;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String token = extractBearerToken(authorizationHeader);

        if (token == null) {
            writeUnauthorizedResponse(response, "Missing or invalid Authorization header");
            return;
        }

        try {
            ClerkJwtVerifier.VerifiedClerkToken verifiedToken = tokenVerifier.verify(token);
            request.setAttribute(AUTHENTICATED_CLERK_USER_ID_ATTRIBUTE, verifiedToken.subject());
            filterChain.doFilter(request, response);
        } catch (ClerkAuthenticationException ex) {
            log.warn("Authentication failed for {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
            writeUnauthorizedResponse(response, ex.getMessage());
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!authProperties.isEnabled()) {
            return true;
        }

        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        return !path.startsWith("/api/v1/");
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }

        if (!authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }

        String token = authorizationHeader.substring(7).trim();
        return token.isEmpty() ? null : token;
    }

    private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        objectMapper.writeValue(response.getWriter(), new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), message));
    }
}
