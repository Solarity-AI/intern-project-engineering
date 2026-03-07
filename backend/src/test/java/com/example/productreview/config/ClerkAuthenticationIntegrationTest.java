package com.example.productreview.config;

import com.example.productreview.BaseIntegrationTest;
import com.example.productreview.model.UserMapping;
import com.example.productreview.repository.UserMappingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ClerkAuthenticationIntegrationTest extends BaseIntegrationTest {

    private static final List<String> CLIENT_IDENTITY_HEADERS = List.of(
            "X-User-ID",
            "X-Authenticated-User-Id",
            "X-Clerk-User-Id",
            "X-Forwarded-User");

    @Autowired
    private UserMappingRepository userMappingRepository;

    @Test
    void protectedEndpoint_WithoutAuthorization_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Missing or invalid Authorization header"));
    }

    @Test
    void protectedEndpoint_WithExpiredToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                        .header("Authorization", expiredBearerToken("expired-user")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("Authentication token has expired"));
    }

    @Test
    void protectedEndpoint_WithValidToken_ShouldProceed() throws Exception {
        mockMvc.perform(get("/api/v1/products").with(clerkAuth("valid-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void authenticatedRequest_ShouldIgnoreSpoofedUserIdHeader() throws Exception {
        mockMvc.perform(post("/api/v1/user/wishlist/1")
                        .with(clerkAuth("real-user"))
                        .header("X-User-ID", "spoofed-user"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/user/wishlist")
                        .with(clerkAuth("real-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(1));

        mockMvc.perform(get("/api/v1/user/wishlist")
                        .with(clerkAuth("spoofed-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void spoofedIdentityHeaders_WithoutAuthorization_ShouldStillReturn401() throws Exception {
        for (String headerName : CLIENT_IDENTITY_HEADERS) {
            mockMvc.perform(get("/api/v1/user/wishlist")
                            .header(headerName, "spoofed-user"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(401))
                    .andExpect(jsonPath("$.message").value("Missing or invalid Authorization header"));
        }
    }

    @Test
    void spoofedIdentityHeaders_WithValidAuthorization_ShouldBeIgnoredAcrossNotificationFlow() throws Exception {
        String tokenOwner = "multi-header-owner";

        for (String headerName : CLIENT_IDENTITY_HEADERS) {
            mockMvc.perform(post("/api/v1/user/notifications")
                            .with(clerkAuth(tokenOwner))
                            .header(headerName, "spoofed-user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "title": "Spoof test: %s",
                                      "message": "Identity must come only from the validated Clerk token."
                                    }
                                    """.formatted(headerName)))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/api/v1/user/notifications")
                        .with(clerkAuth(tokenOwner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(CLIENT_IDENTITY_HEADERS.size())));

        mockMvc.perform(get("/api/v1/user/notifications")
                        .with(clerkAuth("spoofed-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void reviewCreationEndpoint_WithoutAuthorization_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/v1/products/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "reviewerName": "Auth Test User",
                                  "comment": "This review should never reach business logic without auth.",
                                  "rating": 5
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Missing or invalid Authorization header"));
    }

    @Test
    void wishlistEndpoint_WithoutAuthorization_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/v1/user/wishlist/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Missing or invalid Authorization header"));
    }

    @Test
    void notificationEndpoint_WithoutAuthorization_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/v1/user/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Auth Test Notification",
                                  "message": "This should be blocked before reaching application logic."
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Missing or invalid Authorization header"));
    }

    @Test
    void userSpecificDataEndpoint_WithoutAuthorization_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/products/reviews/voted"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Missing or invalid Authorization header"));
    }

    @Test
    void notificationEndpoints_ShouldResolveIdentityFromVerifiedToken() throws Exception {
        mockMvc.perform(post("/api/v1/user/notifications")
                        .with(clerkAuth("token-owner"))
                        .header("X-User-ID", "spoofed-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Identity Check",
                                  "message": "Identity must come from the Clerk token context."
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/user/notifications")
                        .with(clerkAuth("token-owner")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Identity Check"));

        mockMvc.perform(get("/api/v1/user/notifications")
                        .with(clerkAuth("spoofed-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void reviewVoteEndpoint_WithValidAuthorization_ShouldResolveIdentityFromToken() throws Exception {
        mockMvc.perform(put("/api/v1/products/reviews/1/helpful")
                        .with(clerkAuth("token-review-user"))
                        .header("X-User-ID", "spoofed-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        mockMvc.perform(get("/api/v1/products/reviews/voted")
                        .with(clerkAuth("token-review-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(1));

        mockMvc.perform(get("/api/v1/products/reviews/voted")
                        .with(clerkAuth("spoofed-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void authenticatedUserResolution_ShouldCreateAndReuseInternalUserMapping() throws Exception {
        long mappingCountBeforeRequest = userMappingRepository.count();

        mockMvc.perform(post("/api/v1/user/notifications")
                        .with(clerkAuth("mapped-clerk-user"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Mapping Check",
                                  "message": "Create the internal user mapping."
                                }
                                """))
                .andExpect(status().isOk());

        UserMapping createdMapping = userMappingRepository.findByClerkUserId("mapped-clerk-user")
                .orElseThrow();
        assertNotNull(createdMapping.getInternalUserId());
        assertEquals(mappingCountBeforeRequest + 1, userMappingRepository.count());

        mockMvc.perform(get("/api/v1/user/notifications")
                        .with(clerkAuth("mapped-clerk-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Mapping Check"));

        UserMapping reusedMapping = userMappingRepository.findByClerkUserId("mapped-clerk-user")
                .orElseThrow();
        assertEquals(createdMapping.getInternalUserId(), reusedMapping.getInternalUserId());
        assertEquals(mappingCountBeforeRequest + 1, userMappingRepository.count());
    }
}
