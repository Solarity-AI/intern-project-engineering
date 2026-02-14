package com.example.productreview.controller;

import com.example.productreview.dto.ProductDTO;
import com.example.productreview.exception.ValidationException;
import com.example.productreview.model.AppNotification;
import com.example.productreview.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private static final int MAX_PAGE_SIZE = 100;

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private void validatePagination(int page, int size) {
        if (page < 0) {
            throw new ValidationException("Page index must not be negative");
        }
        if (size < 1) {
            throw new ValidationException("Page size must be at least 1");
        }
        if (size > MAX_PAGE_SIZE) {
            throw new ValidationException("Page size must not exceed " + MAX_PAGE_SIZE);
        }
    }

    // --- Wishlist ---

    @Operation(
            tags = "Wishlist",
            summary = "Get wishlist product IDs",
            description = "Returns a list of product IDs in the user's wishlist.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wishlist IDs returned successfully")
    })
    @GetMapping("/wishlist")
    public ResponseEntity<List<Long>> getWishlist(
            @Parameter(description = "User ID", required = true)
            @RequestHeader("X-User-ID") String userId) {
        return ResponseEntity.ok(userService.getWishlist(userId));
    }

    @Operation(
            tags = "Wishlist",
            summary = "Get wishlist products (paginated)",
            description = "Returns a paginated list of full product details for items in the user's wishlist.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page of wishlist products returned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    @GetMapping("/wishlist/products")
    public ResponseEntity<Page<ProductDTO>> getWishlistProducts(
            @Parameter(description = "User ID", required = true)
            @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "Page index (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (1-100)", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field and direction", example = "id,desc")
            @RequestParam(defaultValue = "id,desc") String sort) {

        validatePagination(page, size);

        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc")
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));

        return ResponseEntity.ok(userService.getWishlistProducts(userId, pageable));
    }

    @Operation(
            tags = "Wishlist",
            summary = "Toggle wishlist item",
            description = "Adds the product to the user's wishlist if not present, or removes it if already present.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wishlist toggled successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PostMapping("/wishlist/{productId}")
    public ResponseEntity<Void> toggleWishlist(
            @Parameter(description = "User ID", required = true)
            @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "Product ID to add or remove", example = "1")
            @PathVariable Long productId) {
        userService.toggleWishlist(userId, productId);
        return ResponseEntity.ok().build();
    }

    // --- Notifications ---

    @Operation(
            tags = "Notifications",
            summary = "Get all notifications",
            description = "Returns all notifications for the user, ordered by creation date descending.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notifications returned successfully")
    })
    @GetMapping("/notifications")
    public ResponseEntity<List<AppNotification>> getNotifications(
            @Parameter(description = "User ID", required = true)
            @RequestHeader("X-User-ID") String userId) {
        return ResponseEntity.ok(userService.getNotifications(userId));
    }

    @Operation(
            tags = "Notifications",
            summary = "Get unread notification count",
            description = "Returns the number of unread notifications for the user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unread count returned successfully")
    })
    @GetMapping("/notifications/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @Parameter(description = "User ID", required = true)
            @RequestHeader("X-User-ID") String userId) {
        return ResponseEntity.ok(Map.of("count", userService.getUnreadCount(userId)));
    }

    @Operation(
            tags = "Notifications",
            summary = "Mark notification as read",
            description = "Marks a single notification as read by its ID. The notification must belong to the requesting user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification marked as read"),
            @ApiResponse(responseCode = "403", description = "Notification does not belong to user"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @PutMapping("/notifications/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @Parameter(description = "User ID", required = true)
            @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "Notification ID", example = "1")
            @PathVariable Long id) {
        userService.markAsRead(id, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            tags = "Notifications",
            summary = "Mark all notifications as read",
            description = "Marks all of the user's notifications as read.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "All notifications marked as read")
    })
    @PutMapping("/notifications/read-all")
    public ResponseEntity<Void> markAllAsRead(
            @Parameter(description = "User ID", required = true)
            @RequestHeader("X-User-ID") String userId) {
        userService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            tags = "Notifications",
            summary = "Create a notification",
            description = "Creates a new notification for the user with a title, message, and optional product link.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid notification data")
    })
    @PostMapping("/notifications")
    public ResponseEntity<Void> createNotification(
            @Parameter(description = "User ID", required = true)
            @RequestHeader("X-User-ID") String userId,
            @RequestBody Map<String, Object> payload) {

        String title = (String) payload.get("title");
        String message = (String) payload.get("message");
        Long productId = payload.get("productId") != null ? ((Number) payload.get("productId")).longValue() : null;

        userService.createNotification(userId, title, message, productId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            tags = "Notifications",
            summary = "Delete a notification",
            description = "Permanently deletes a single notification by its ID. The notification must belong to the requesting user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Notification does not belong to user"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @DeleteMapping("/notifications/{id}")
    public ResponseEntity<Void> deleteNotification(
            @Parameter(description = "User ID", required = true)
            @RequestHeader("X-User-ID") String userId,
            @Parameter(description = "Notification ID", example = "1")
            @PathVariable Long id) {
        userService.deleteNotification(id, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            tags = "Notifications",
            summary = "Delete all notifications",
            description = "Permanently deletes all notifications for the user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "All notifications deleted successfully")
    })
    @DeleteMapping("/notifications")
    public ResponseEntity<Void> deleteAllNotifications(
            @Parameter(description = "User ID", required = true)
            @RequestHeader("X-User-ID") String userId) {
        userService.deleteAllNotifications(userId);
        return ResponseEntity.ok().build();
    }
}
