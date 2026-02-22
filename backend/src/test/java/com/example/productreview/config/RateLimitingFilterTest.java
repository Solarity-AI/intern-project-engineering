package com.example.productreview.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RateLimitingFilterTest {

    private RateLimitingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitingFilter();
        ReflectionTestUtils.setField(filter, "requestsPerMinute", 2);
    }

    @Test
    void doFilterInternal_WithinLimit_ShouldAllowRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/products");
        request.setRemoteAddr("192.168.1.1");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotEquals(429, response.getStatus());
    }

    @Test
    void doFilterInternal_ExceedingLimit_ShouldReturn429() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/products");
        request.setRemoteAddr("10.0.0.1");

        for (int i = 0; i < 2; i++) {
            filter.doFilterInternal(request, new MockHttpServletResponse(), mock(FilterChain.class));
        }

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);
        filter.doFilterInternal(request, response, filterChain);

        assertEquals(429, response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ExceedingLimit_ShouldReturnJsonBody() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/products");
        request.setRemoteAddr("10.0.0.2");

        for (int i = 0; i < 2; i++) {
            filter.doFilterInternal(request, new MockHttpServletResponse(), mock(FilterChain.class));
        }

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(request, response, mock(FilterChain.class));

        assertEquals("application/json", response.getContentType());
        String body = response.getContentAsString();
        assertTrue(body.contains("Too many requests"));
        assertTrue(body.contains("429"));
    }

    @Test
    void resolveClientId_WithXForwardedFor_ShouldUseFirstIp() throws Exception {
        MockHttpServletRequest request1 = new MockHttpServletRequest();
        request1.setRequestURI("/api/v1/products");
        request1.setRemoteAddr("127.0.0.1");
        request1.addHeader("X-Forwarded-For", "203.0.113.1, 70.41.3.18");

        MockHttpServletRequest request2 = new MockHttpServletRequest();
        request2.setRequestURI("/api/v1/products");
        request2.setRemoteAddr("127.0.0.2");
        request2.addHeader("X-Forwarded-For", "203.0.113.1, 10.0.0.1");

        // Exhaust the bucket via request1
        for (int i = 0; i < 2; i++) {
            filter.doFilterInternal(request1, new MockHttpServletResponse(), mock(FilterChain.class));
        }

        // request2 shares the same client ID (first X-Forwarded-For IP)
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(request2, response, mock(FilterChain.class));

        assertEquals(429, response.getStatus());
    }

    @Test
    void resolveClientId_WithoutForwardedHeader_ShouldUseRemoteAddr() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/products");
        request.setRemoteAddr("192.168.1.100");

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotFilter_ActuatorPath_ShouldReturnTrue() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/actuator/health");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_H2ConsolePath_ShouldReturnTrue() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/h2-console");
        assertTrue(filter.shouldNotFilter(request));
    }

    @Test
    void shouldNotFilter_ApiPath_ShouldReturnFalse() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/products");
        assertFalse(filter.shouldNotFilter(request));
    }
}
