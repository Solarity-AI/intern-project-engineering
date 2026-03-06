package com.example.productreview.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ClerkAuthenticationFilterTest {

    private ClerkAuthProperties authProperties;
    private ClerkJwtVerifier verifier;
    private ClerkAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        authProperties = new ClerkAuthProperties();
        authProperties.setEnabled(true);
        authProperties.setVerificationKey("unused-in-filter-unit-test");
        verifier = mock(ClerkJwtVerifier.class);
        filter = new ClerkAuthenticationFilter(
                authProperties,
                verifier,
                Jackson2ObjectMapperBuilder.json().build());
    }

    @Test
    void doFilterInternal_WithoutAuthorizationHeader_ShouldReturn401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/products");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, mock(FilterChain.class));

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Missing or invalid Authorization header"));
        verifyNoInteractions(verifier);
    }

    @Test
    void doFilterInternal_WithSpoofedIdentityHeadersAndNoAuthorization_ShouldReturn401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/user/wishlist");
        request.addHeader("X-User-ID", "spoofed-user");
        request.addHeader("X-Authenticated-User-Id", "spoofed-user");
        request.addHeader("X-Clerk-User-Id", "spoofed-user");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, mock(FilterChain.class));

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Missing or invalid Authorization header"));
        assertNull(request.getAttribute(ClerkAuthenticationFilter.AUTHENTICATED_CLERK_USER_ID_ATTRIBUTE));
        verifyNoInteractions(verifier);
    }

    @Test
    void doFilterInternal_WithValidToken_ShouldInjectAuthenticatedUserId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/user/wishlist");
        request.addHeader("Authorization", "Bearer valid-token");
        request.addHeader("X-User-ID", "spoofed-user");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        when(verifier.verify("valid-token"))
                .thenReturn(new ClerkJwtVerifier.VerifiedClerkToken("real-user"));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(same(request), eq(response));
        assertEquals("real-user", request.getAttribute(ClerkAuthenticationFilter.AUTHENTICATED_CLERK_USER_ID_ATTRIBUTE));
        assertEquals("spoofed-user", request.getHeader("X-User-ID"));
    }

    @Test
    void doFilterInternal_WithExpiredToken_ShouldReturn401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/products");
        request.addHeader("Authorization", "Bearer expired-token");

        MockHttpServletResponse response = new MockHttpServletResponse();

        when(verifier.verify("expired-token"))
                .thenThrow(new ClerkAuthenticationException("Authentication token has expired"));

        filter.doFilterInternal(request, response, mock(FilterChain.class));

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("Authentication token has expired"));
    }

    @Test
    void shouldNotFilter_OptionsRequest_ShouldReturnTrue() {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/v1/products");

        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_NonApiPath_ShouldReturnTrue() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");

        assertTrue(filter.shouldNotFilter(request));
    }
}
