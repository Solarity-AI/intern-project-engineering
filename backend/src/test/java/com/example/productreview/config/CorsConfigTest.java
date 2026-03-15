package com.example.productreview.config;

import com.example.productreview.BaseIntegrationTest;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CorsConfigTest extends BaseIntegrationTest {

    @Test
    void allowedOrigin_shouldReturnCorsHeaders() throws Exception {
        mockMvc.perform(options("/api/v1/products")
                        .header("Origin", "http://localhost:19006")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:19006"));
    }

    @Test
    void disallowedOrigin_shouldNotReturnCorsHeaders() throws Exception {
        mockMvc.perform(options("/api/v1/products")
                        .header("Origin", "https://evil-site.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void allowedOrigin_actualRequest_shouldIncludeCorsHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .with(clerkAuth("cors-user"))
                        .header("Origin", "http://localhost:8081"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:8081"));
    }

    @Test
    void allowedOrigin_unauthorizedActualRequest_shouldStillIncludeCorsHeaders() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .header("Origin", "http://localhost:8081"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:8081"));
    }
}
