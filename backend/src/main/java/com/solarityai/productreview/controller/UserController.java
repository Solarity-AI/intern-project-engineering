package com.solarityai.productreview.controller;

import com.solarityai.backendfw.foundation.controller.BaseController;
import com.solarityai.backendfw.query.model.PageRequestDto;
import com.solarityai.productreview.dto.AppNotificationDto;
import com.solarityai.productreview.dto.NotificationCreateDto;
import com.solarityai.productreview.dto.ProductDto;
import com.solarityai.productreview.dto.UnreadCountDto;
import com.solarityai.productreview.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController extends BaseController {

    private final UserService userService;

    // Wishlist endpoints
    @GetMapping("/wishlist")
    public ResponseEntity<List<UUID>> getWishlistProductIds(
            @RequestHeader(value = "X-User-ID", required = true) String userId) {
        List<UUID> productIds = userService.getWishlistProductIds(userId);
        return ResponseEntity.ok(productIds);
    }

    @GetMapping("/wishlist/products")
    public ResponseEntity<Page<ProductDto>> getWishlistProducts(
            @RequestHeader(value = "X-User-ID", required = true) String userId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "name,asc") String sort) {

        PageRequestDto pageRequest = PageRequestDto.builder()
                .page(page)
                .size(size)
                .build();

        Page<ProductDto> products = userService.getWishlistProducts(userId, pageRequest);
        return ResponseEntity.ok(products);
    }

    @PostMapping("/wishlist/{productId}")
    public ResponseEntity<Void> toggleWishlistItem(
            @RequestHeader(value = "X-User-ID", required = true) String userId,
            @PathVariable UUID productId) {
        userService.toggleWishlistItem(userId, productId);
        return ResponseEntity.ok().build();
    }

    // Notification endpoints
    @GetMapping("/notifications")
    public ResponseEntity<List<AppNotificationDto>> getNotifications(
            @RequestHeader(value = "X-User-ID", required = true) String userId) {
        List<AppNotificationDto> notifications = userService.getNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/notifications/unread-count")
    public ResponseEntity<UnreadCountDto> getUnreadNotificationCount(
            @RequestHeader(value = "X-User-ID", required = true) String userId) {
        UnreadCountDto count = userService.getUnreadNotificationCount(userId);
        return ResponseEntity.ok(count);
    }

    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable UUID id) {
        userService.markNotificationAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/notifications/read-all")
    public ResponseEntity<Void> markAllNotificationsAsRead(
            @RequestHeader(value = "X-User-ID", required = true) String userId) {
        userService.markAllNotificationsAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/notifications")
    public ResponseEntity<AppNotificationDto> createNotification(
            @RequestHeader(value = "X-User-ID", required = true) String userId,
            @Valid @RequestBody NotificationCreateDto dto) {
        AppNotificationDto notification = userService.createNotification(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(notification);
    }

    @DeleteMapping("/notifications/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable UUID id) {
        userService.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/notifications")
    public ResponseEntity<Void> deleteAllNotifications(
            @RequestHeader(value = "X-User-ID", required = true) String userId) {
        userService.deleteAllNotifications(userId);
        return ResponseEntity.noContent().build();
    }
}
