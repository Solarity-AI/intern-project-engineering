package com.example.productreview.controller;

import com.example.productreview.model.AppNotification;
import com.example.productreview.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // --- Wishlist ---

    @GetMapping("/wishlist")
    public ResponseEntity<List<Long>> getWishlist(@RequestHeader("X-User-ID") String userId) {
        return ResponseEntity.ok(userService.getWishlist(userId));
    }

    @PostMapping("/wishlist/{productId}")
    public ResponseEntity<Void> toggleWishlist(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable Long productId) {
        userService.toggleWishlist(userId, productId);
        return ResponseEntity.ok().build();
    }

    // --- Notifications ---

    @GetMapping("/notifications")
    public ResponseEntity<List<AppNotification>> getNotifications(@RequestHeader("X-User-ID") String userId) {
        return ResponseEntity.ok(userService.getNotifications(userId));
    }
    
    @GetMapping("/notifications/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@RequestHeader("X-User-ID") String userId) {
        return ResponseEntity.ok(Map.of("count", userService.getUnreadCount(userId)));
    }

    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        userService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/notifications/read-all")
    public ResponseEntity<Void> markAllAsRead(@RequestHeader("X-User-ID") String userId) {
        userService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
    
    // Test endpoint to create a notification manually
    @PostMapping("/notifications")
    public ResponseEntity<Void> createNotification(
            @RequestHeader("X-User-ID") String userId,
            @RequestBody Map<String, Object> payload) {
        
        String title = (String) payload.get("title");
        String message = (String) payload.get("message");
        Long productId = payload.get("productId") != null ? ((Number) payload.get("productId")).longValue() : null;
        
        userService.createNotification(userId, title, message, productId);
        return ResponseEntity.ok().build();
    }
}
