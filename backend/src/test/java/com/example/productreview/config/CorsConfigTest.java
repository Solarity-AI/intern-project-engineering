package com.example.productreview.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "rate-limit.requests-per-minute=10000")
class CorsConfigTest {

    @Autowired
    private MockMvc mockMvc;

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
                        .header("Origin", "http://localhost:8081"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:8081"));
    }
}
