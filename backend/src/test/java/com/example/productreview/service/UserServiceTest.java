package com.example.productreview.service;

import com.example.productreview.dto.ProductDTO;
import com.example.productreview.exception.ResourceNotFoundException;
import com.example.productreview.exception.UnauthorizedException;
import com.example.productreview.model.AppNotification;
import com.example.productreview.model.Product;
import com.example.productreview.model.WishlistItem;
import com.example.productreview.repository.NotificationRepository;
import com.example.productreview.repository.ProductRepository;
import com.example.productreview.repository.WishlistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private static final String USER_ID = "test-user-123";

    // --- Wishlist Tests ---

    @Test
    void getWishlist_ShouldReturnProductIds() {
        when(wishlistRepository.findProductIdsByUserId(USER_ID)).thenReturn(Arrays.asList(1L, 2L));

        List<Long> result = userService.getWishlist(USER_ID);

        assertEquals(2, result.size());
        assertTrue(result.contains(1L));
        assertTrue(result.contains(2L));
    }

    @Test
    void getWishlist_WhenEmpty_ShouldReturnEmptyList() {
        when(wishlistRepository.findProductIdsByUserId(USER_ID)).thenReturn(Collections.emptyList());

        List<Long> result = userService.getWishlist(USER_ID);

        assertTrue(result.isEmpty());
    }

    @Test
    void toggleWishlist_WhenNotInWishlist_ShouldAdd() {
        Product p = new Product();
        p.setId(1L);
        p.setName("Test");
        p.setCategories(new HashSet<>());
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        when(wishlistRepository.findByUserIdAndProductId(USER_ID, 1L)).thenReturn(Optional.empty());

        userService.toggleWishlist(USER_ID, 1L);

        verify(wishlistRepository).save(any(WishlistItem.class));
        verify(wishlistRepository, never()).delete(any());
    }

    @Test
    void toggleWishlist_WhenInWishlist_ShouldRemove() {
        Product p = new Product();
        p.setId(1L);
        p.setName("Test");
        p.setCategories(new HashSet<>());
        when(productRepository.findById(1L)).thenReturn(Optional.of(p));
        WishlistItem existing = new WishlistItem(USER_ID, 1L);
        when(wishlistRepository.findByUserIdAndProductId(USER_ID, 1L)).thenReturn(Optional.of(existing));

        userService.toggleWishlist(USER_ID, 1L);

        verify(wishlistRepository).delete(existing);
        verify(wishlistRepository, never()).save(any());
    }

    @Test
    void toggleWishlist_WhenProductNotFound_ShouldThrowResourceNotFoundException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.toggleWishlist(USER_ID, 999L));
    }

    @Test
    void getWishlistProducts_ShouldReturnPagedProducts() {
        when(wishlistRepository.findProductIdsByUserId(USER_ID)).thenReturn(Arrays.asList(1L));

        Product p = new Product();
        p.setId(1L);
        p.setName("Test Product");
        p.setDescription("Desc");
        p.setCategories(new HashSet<>(Arrays.asList("Electronics")));
        p.setPrice(100.0);

        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.findByIdIn(Arrays.asList(1L), pageable))
                .thenReturn(new PageImpl<>(Arrays.asList(p)));

        Page<ProductDTO> result = userService.getWishlistProducts(USER_ID, pageable);

        assertEquals(1, result.getContent().size());
        assertEquals("Test Product", result.getContent().get(0).getName());
    }

    @Test
    void getWishlistProducts_WhenEmpty_ShouldReturnEmptyPage() {
        when(wishlistRepository.findProductIdsByUserId(USER_ID)).thenReturn(Collections.emptyList());

        Pageable pageable = PageRequest.of(0, 10);

        Page<ProductDTO> result = userService.getWishlistProducts(USER_ID, pageable);

        assertTrue(result.getContent().isEmpty());
        verify(productRepository, never()).findByIdIn(anyList(), any(Pageable.class));
    }

    // --- Notification Tests ---

    @Test
    void getNotifications_ShouldReturnOrderedNotifications() {
        AppNotification n1 = new AppNotification(USER_ID, "Title1", "Msg1", null);
        AppNotification n2 = new AppNotification(USER_ID, "Title2", "Msg2", 1L);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(USER_ID))
                .thenReturn(Arrays.asList(n2, n1));

        List<AppNotification> result = userService.getNotifications(USER_ID);

        assertEquals(2, result.size());
        assertEquals("Title2", result.get(0).getTitle());
    }

    @Test
    void getUnreadCount_ShouldReturnCount() {
        when(notificationRepository.countByUserIdAndIsReadFalse(USER_ID)).thenReturn(3L);

        long count = userService.getUnreadCount(USER_ID);

        assertEquals(3, count);
    }

    @Test
    void markAsRead_ShouldSetReadTrue() {
        AppNotification notification = new AppNotification(USER_ID, "Title", "Msg", null);
        notification.setId(1L);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        userService.markAsRead(1L, USER_ID);

        assertTrue(notification.isRead());
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsRead_WhenNotFound_ShouldThrowException() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.markAsRead(999L, USER_ID));
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markAsRead_WrongUser_ShouldThrowUnauthorized() {
        AppNotification notification = new AppNotification(USER_ID, "Title", "Msg", null);
        notification.setId(1L);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        assertThrows(UnauthorizedException.class, () -> userService.markAsRead(1L, "wrong-user"));
        assertFalse(notification.isRead());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markAllAsRead_ShouldMarkAllUnreadAsRead() {
        when(notificationRepository.markAllAsReadByUserId(USER_ID)).thenReturn(2);

        userService.markAllAsRead(USER_ID);

        verify(notificationRepository).markAllAsReadByUserId(USER_ID);
    }

    @Test
    void createNotification_ShouldSaveNotification() {
        userService.createNotification(USER_ID, "New Title", "New Message", 1L);

        verify(notificationRepository).save(any(AppNotification.class));
    }

    @Test
    void deleteNotification_WhenExists_ShouldDelete() {
        AppNotification notification = new AppNotification(USER_ID, "Title", "Msg", null);
        notification.setId(1L);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        userService.deleteNotification(1L, USER_ID);

        verify(notificationRepository).delete(notification);
    }

    @Test
    void deleteNotification_WhenNotExists_ShouldThrowException() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteNotification(999L, USER_ID));
        verify(notificationRepository, never()).delete(any());
    }

    @Test
    void deleteNotification_WrongUser_ShouldThrowUnauthorized() {
        AppNotification notification = new AppNotification(USER_ID, "Title", "Msg", null);
        notification.setId(1L);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));

        assertThrows(UnauthorizedException.class, () -> userService.deleteNotification(1L, "wrong-user"));
        verify(notificationRepository, never()).delete(any());
    }

    @Test
    void deleteAllNotifications_ShouldDeleteAllForUser() {
        when(notificationRepository.deleteAllByUserId(USER_ID)).thenReturn(1);

        userService.deleteAllNotifications(USER_ID);

        verify(notificationRepository).deleteAllByUserId(USER_ID);
    }

    // --- Additional Tests for Criteria Compliance ---

    @Test
    void getNotifications_WhenEmpty_ShouldReturnEmptyList() {
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(USER_ID))
                .thenReturn(Collections.emptyList());

        List<AppNotification> result = userService.getNotifications(USER_ID);

        assertTrue(result.isEmpty());
    }

    @Test
    void getUnreadCount_WhenNoNotifications_ShouldReturnZero() {
        when(notificationRepository.countByUserIdAndIsReadFalse(USER_ID)).thenReturn(0L);

        long count = userService.getUnreadCount(USER_ID);

        assertEquals(0, count);
    }

    @Test
    void createNotification_WithNullProductId_ShouldWork() {
        userService.createNotification(USER_ID, "Title", "Message", null);

        verify(notificationRepository).save(any(AppNotification.class));
    }

    @Test
    void markAllAsRead_WhenAllAlreadyRead_ShouldStillCallBulkUpdate() {
        when(notificationRepository.markAllAsReadByUserId(USER_ID)).thenReturn(0);

        userService.markAllAsRead(USER_ID);

        verify(notificationRepository).markAllAsReadByUserId(USER_ID);
    }

    @Test
    void deleteAllNotifications_WhenEmpty_ShouldStillCallBulkDelete() {
        when(notificationRepository.deleteAllByUserId(USER_ID)).thenReturn(0);

        userService.deleteAllNotifications(USER_ID);

        verify(notificationRepository).deleteAllByUserId(USER_ID);
    }

    @Test
    void getWishlist_WithMultipleItems_ShouldReturnAllIds() {
        when(wishlistRepository.findProductIdsByUserId(USER_ID))
                .thenReturn(Arrays.asList(10L, 20L, 30L));

        List<Long> result = userService.getWishlist(USER_ID);

        assertEquals(3, result.size());
        assertTrue(result.containsAll(Arrays.asList(10L, 20L, 30L)));
    }

    @Test
    void getWishlistProducts_WithMultipleIds_ShouldReturnPagedProducts() {
        when(wishlistRepository.findProductIdsByUserId(USER_ID)).thenReturn(Arrays.asList(1L, 2L));

        Product p1 = new Product();
        p1.setId(1L);
        p1.setName("Product 1");
        p1.setDescription("Desc1");
        p1.setCategories(new HashSet<>());
        p1.setPrice(50.0);

        Product p2 = new Product();
        p2.setId(2L);
        p2.setName("Product 2");
        p2.setDescription("Desc2");
        p2.setCategories(new HashSet<>());
        p2.setPrice(75.0);

        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.findByIdIn(Arrays.asList(1L, 2L), pageable))
                .thenReturn(new PageImpl<>(Arrays.asList(p1, p2)));

        Page<ProductDTO> result = userService.getWishlistProducts(USER_ID, pageable);

        assertEquals(2, result.getContent().size());
    }
}
