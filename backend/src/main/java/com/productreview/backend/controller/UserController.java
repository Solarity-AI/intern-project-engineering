package com.productreview.backend.controller;

import com.productreview.backend.dto.NotificationCreateRequest;
import com.productreview.backend.dto.ProductDTO;
import com.productreview.backend.dto.UnreadCountResponse;
import com.productreview.backend.entity.AppNotification;
import com.productreview.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // --- Wishlist ---

    @GetMapping("/wishlist")
    public ResponseEntity<List<Long>> getWishlist(
            @RequestHeader("X-User-ID") String userId) {
        return ResponseEntity.ok(userService.getWishlist(userId));
    }

    @GetMapping("/wishlist/products")
    public ResponseEntity<Page<ProductDTO>> getWishlistProducts(
            @RequestHeader("X-User-ID") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,desc") String sort) {

        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

        return ResponseEntity.ok(userService.getWishlistProducts(userId, pageable));
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
    public ResponseEntity<List<AppNotification>> getNotifications(
            @RequestHeader("X-User-ID") String userId) {
        return ResponseEntity.ok(userService.getNotifications(userId));
    }

    @GetMapping("/notifications/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @RequestHeader("X-User-ID") String userId) {
        return ResponseEntity.ok(new UnreadCountResponse((int) userService.getUnreadCount(userId)));
    }

    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        userService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/notifications/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @RequestHeader("X-User-ID") String userId) {
        userService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/notifications")
    public ResponseEntity<Void> createNotification(
            @RequestHeader("X-User-ID") String userId,
            @RequestBody NotificationCreateRequest request) {
        userService.createNotification(userId, request.getTitle(), request.getMessage(), request.getProductId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/notifications/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        userService.deleteNotification(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/notifications")
    public ResponseEntity<Void> deleteAllNotifications(
            @RequestHeader("X-User-ID") String userId) {
        userService.deleteAllNotifications(userId);
        return ResponseEntity.ok().build();
    }
}
