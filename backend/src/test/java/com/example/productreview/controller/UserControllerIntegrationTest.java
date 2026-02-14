package com.example.productreview.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // --- Wishlist Endpoints ---

    @Test
    void getWishlist_WithValidHeader_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/v1/user/wishlist")
                .header("X-User-ID", "wishlist-get-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getWishlist_WithoutHeader_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/user/wishlist"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void toggleWishlist_ShouldAddAndRemoveProduct() throws Exception {
        String userId = "wishlist-toggle-user";

        // Add to wishlist
        mockMvc.perform(post("/api/v1/user/wishlist/1")
                .header("X-User-ID", userId))
                .andExpect(status().isOk());

        // Verify it's in the wishlist
        mockMvc.perform(get("/api/v1/user/wishlist")
                .header("X-User-ID", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(1));

        // Toggle again to remove
        mockMvc.perform(post("/api/v1/user/wishlist/1")
                .header("X-User-ID", userId))
                .andExpect(status().isOk());

        // Verify it's removed
        mockMvc.perform(get("/api/v1/user/wishlist")
                .header("X-User-ID", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getWishlistProducts_ShouldReturnPagedProducts() throws Exception {
        String userId = "wishlist-products-user";

        // Add product to wishlist
        mockMvc.perform(post("/api/v1/user/wishlist/1")
                .header("X-User-ID", userId))
                .andExpect(status().isOk());

        // Get wishlist products
        mockMvc.perform(get("/api/v1/user/wishlist/products")
                .header("X-User-ID", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    void toggleWishlist_WithoutHeader_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/user/wishlist/1"))
                .andExpect(status().isBadRequest());
    }

    // --- Notification Endpoints ---

    @Test
    void createAndGetNotifications_ShouldWork() throws Exception {
        String userId = "notif-create-user";

        mockMvc.perform(post("/api/v1/user/notifications")
                .header("X-User-ID", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        Map.of("title", "Test Notification", "message", "This is a test"))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/user/notifications")
                .header("X-User-ID", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Notification"))
                .andExpect(jsonPath("$[0].message").value("This is a test"))
                .andExpect(jsonPath("$[0].read").value(false));
    }

    @Test
    void getUnreadCount_ShouldReturnCorrectCount() throws Exception {
        String userId = "notif-count-user";

        // Create two notifications
        for (String title : List.of("Notif 1", "Notif 2")) {
            mockMvc.perform(post("/api/v1/user/notifications")
                    .header("X-User-ID", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of("title", title, "message", "msg"))))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/api/v1/user/notifications/unread-count")
                .header("X-User-ID", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2));
    }

    @Test
    void markAsRead_ShouldUpdateNotification() throws Exception {
        String userId = "notif-read-user";

        // Create notification
        mockMvc.perform(post("/api/v1/user/notifications")
                .header("X-User-ID", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("title", "Read Test", "message", "msg"))))
                .andExpect(status().isOk());

        // Get notification ID
        String response = mockMvc.perform(get("/api/v1/user/notifications")
                .header("X-User-ID", userId))
                .andReturn().getResponse().getContentAsString();

        List<?> notifications = objectMapper.readValue(response, List.class);
        int notificationId = (int) ((Map<?, ?>) notifications.get(0)).get("id");

        // Mark as read
        mockMvc.perform(put("/api/v1/user/notifications/" + notificationId + "/read")
                .header("X-User-ID", userId))
                .andExpect(status().isOk());

        // Verify unread count is 0
        mockMvc.perform(get("/api/v1/user/notifications/unread-count")
                .header("X-User-ID", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    void markAllAsRead_ShouldMarkAllNotificationsAsRead() throws Exception {
        String userId = "notif-readall-user";

        // Create two notifications
        for (String title : List.of("First", "Second")) {
            mockMvc.perform(post("/api/v1/user/notifications")
                    .header("X-User-ID", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of("title", title, "message", "msg"))))
                    .andExpect(status().isOk());
        }

        // Mark all as read
        mockMvc.perform(put("/api/v1/user/notifications/read-all")
                .header("X-User-ID", userId))
                .andExpect(status().isOk());

        // Verify unread count is 0
        mockMvc.perform(get("/api/v1/user/notifications/unread-count")
                .header("X-User-ID", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    void deleteNotification_ShouldRemoveNotification() throws Exception {
        String userId = "notif-delete-user";

        // Create notification
        mockMvc.perform(post("/api/v1/user/notifications")
                .header("X-User-ID", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("title", "Delete Me", "message", "msg"))))
                .andExpect(status().isOk());

        // Get notification ID
        String response = mockMvc.perform(get("/api/v1/user/notifications")
                .header("X-User-ID", userId))
                .andReturn().getResponse().getContentAsString();

        List<?> notifications = objectMapper.readValue(response, List.class);
        int notificationId = (int) ((Map<?, ?>) notifications.get(0)).get("id");

        // Delete
        mockMvc.perform(delete("/api/v1/user/notifications/" + notificationId)
                .header("X-User-ID", userId))
                .andExpect(status().isOk());

        // Verify it's gone
        mockMvc.perform(get("/api/v1/user/notifications")
                .header("X-User-ID", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void deleteAllNotifications_ShouldRemoveAllForUser() throws Exception {
        String userId = "notif-deleteall-user";

        // Create two notifications
        for (String title : List.of("First", "Second")) {
            mockMvc.perform(post("/api/v1/user/notifications")
                    .header("X-User-ID", userId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(Map.of("title", title, "message", "msg"))))
                    .andExpect(status().isOk());
        }

        // Delete all
        mockMvc.perform(delete("/api/v1/user/notifications")
                .header("X-User-ID", userId))
                .andExpect(status().isOk());

        // Verify all gone
        mockMvc.perform(get("/api/v1/user/notifications")
                .header("X-User-ID", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getNotifications_WithoutHeader_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/user/notifications"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createNotification_WithProductId_ShouldWork() throws Exception {
        String userId = "notif-product-user";

        mockMvc.perform(post("/api/v1/user/notifications")
                .header("X-User-ID", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        Map.of("title", "Review Posted", "message", "Your review was posted", "productId", 1))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/user/notifications")
                .header("X-User-ID", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(1));
    }

    // --- Additional Tests for Criteria Compliance ---

    @Test
    void toggleWishlist_WithNonExistentProduct_ShouldStillReturn200() throws Exception {
        String userId = "wishlist-nonexist-user";

        mockMvc.perform(post("/api/v1/user/wishlist/99999")
                .header("X-User-ID", userId))
                .andExpect(status().isOk());
    }

    @Test
    void getWishlistProducts_WithPagination_ShouldRespectParams() throws Exception {
        String userId = "wishlist-page-user";

        mockMvc.perform(get("/api/v1/user/wishlist/products")
                .header("X-User-ID", userId)
                .param("page", "0")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(5));
    }

    @Test
    void markAsRead_WithNonExistentId_ShouldReturn404() throws Exception {
        mockMvc.perform(put("/api/v1/user/notifications/99999/read")
                .header("X-User-ID", "some-user"))
                .andExpect(status().isNotFound());
    }

    @Test
    void markAsRead_WithoutHeader_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/api/v1/user/notifications/1/read"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteNotification_WithNonExistentId_ShouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/v1/user/notifications/99999")
                .header("X-User-ID", "some-user"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteNotification_WithoutHeader_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(delete("/api/v1/user/notifications/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void markAsRead_WrongUser_ShouldReturnUnauthorized() throws Exception {
        String ownerId = "notif-owner-markread";

        // Create notification as owner
        mockMvc.perform(post("/api/v1/user/notifications")
                .header("X-User-ID", ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("title", "Owner Only", "message", "msg"))))
                .andExpect(status().isOk());

        // Get notification ID
        String response = mockMvc.perform(get("/api/v1/user/notifications")
                .header("X-User-ID", ownerId))
                .andReturn().getResponse().getContentAsString();

        List<?> notifications = objectMapper.readValue(response, List.class);
        int notificationId = (int) ((Map<?, ?>) notifications.get(0)).get("id");

        // Try to mark as read with a different user
        mockMvc.perform(put("/api/v1/user/notifications/" + notificationId + "/read")
                .header("X-User-ID", "different-user"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteNotification_WrongUser_ShouldReturnUnauthorized() throws Exception {
        String ownerId = "notif-owner-delete";

        // Create notification as owner
        mockMvc.perform(post("/api/v1/user/notifications")
                .header("X-User-ID", ownerId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("title", "Owner Only", "message", "msg"))))
                .andExpect(status().isOk());

        // Get notification ID
        String response = mockMvc.perform(get("/api/v1/user/notifications")
                .header("X-User-ID", ownerId))
                .andReturn().getResponse().getContentAsString();

        List<?> notifications = objectMapper.readValue(response, List.class);
        int notificationId = (int) ((Map<?, ?>) notifications.get(0)).get("id");

        // Try to delete with a different user
        mockMvc.perform(delete("/api/v1/user/notifications/" + notificationId)
                .header("X-User-ID", "different-user"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getNotifications_ShouldReturnEmptyArrayForNewUser() throws Exception {
        mockMvc.perform(get("/api/v1/user/notifications")
                .header("X-User-ID", "brand-new-user-with-no-data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getUnreadCount_ForNewUser_ShouldReturnZero() throws Exception {
        mockMvc.perform(get("/api/v1/user/notifications/unread-count")
                .header("X-User-ID", "fresh-user-zero-notifs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    void markAllAsRead_WithoutHeader_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(put("/api/v1/user/notifications/read-all"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteAllNotifications_WithoutHeader_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(delete("/api/v1/user/notifications"))
                .andExpect(status().isBadRequest());
    }
}
