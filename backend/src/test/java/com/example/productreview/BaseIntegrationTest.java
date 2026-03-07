package com.example.productreview;

import com.example.productreview.support.TestClerkJwtSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "rate-limit.requests-per-minute=10000")
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @DynamicPropertySource
    static void registerClerkAuthProperties(DynamicPropertyRegistry registry) {
        registry.add("clerk.auth.enabled", () -> "true");
        registry.add("clerk.auth.verification-key", TestClerkJwtSupport::publicKeyPem);
    }

    protected RequestPostProcessor clerkAuth() {
        return clerkAuth("integration-test-user");
    }

    protected RequestPostProcessor clerkAuth(String userId) {
        return request -> {
            request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + TestClerkJwtSupport.validToken(userId));
            return request;
        };
    }

    protected String bearerToken(String userId) {
        return "Bearer " + TestClerkJwtSupport.validToken(userId);
    }

    protected String expiredBearerToken(String userId) {
        return "Bearer " + TestClerkJwtSupport.expiredToken(userId);
    }
}
