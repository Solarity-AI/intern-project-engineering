package com.example.productreview.service;

import com.example.productreview.dto.ProductDTO;
import com.example.productreview.exception.ResourceNotFoundException;
import com.example.productreview.exception.UnauthorizedException;
import com.example.productreview.model.AppNotification;
import com.example.productreview.model.WishlistItem;
import com.example.productreview.repository.NotificationRepository;
import com.example.productreview.repository.ProductRepository;
import com.example.productreview.repository.WishlistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final WishlistRepository wishlistRepository;
    private final NotificationRepository notificationRepository;
    private final ProductRepository productRepository; // ✨ Added ProductRepository

    public UserService(WishlistRepository wishlistRepository, 
                       NotificationRepository notificationRepository,
                       ProductRepository productRepository) {
        this.wishlistRepository = wishlistRepository;
        this.notificationRepository = notificationRepository;
        this.productRepository = productRepository;
    }

    // --- Wishlist ---

    @Transactional(readOnly = true)
    public List<Long> getWishlist(String userId) {
        return wishlistRepository.findProductIdsByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Page<ProductDTO> getWishlistProducts(String userId, Pageable pageable) {
        List<Long> productIds = wishlistRepository.findProductIdsByUserId(userId);
        if (productIds.isEmpty()) {
            return Page.empty(pageable);
        }
        return productRepository.findByIdIn(productIds, pageable)
                .map(p -> new ProductDTO(
                        p.getId(),
                        p.getName(),
                        p.getDescription(),
                        p.getCategories(),
                        p.getPrice(),
                        p.getImageUrl(),
                        p.getAverageRating(),
                        p.getReviewCount(),
                        null,
                        null
                ));
    }

    @Transactional
    public void toggleWishlist(String userId, Long productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        var existing = wishlistRepository.findByUserIdAndProductId(userId, productId);
        if (existing.isPresent()) {
            wishlistRepository.delete(existing.get());
        } else {
            try {
                wishlistRepository.save(new WishlistItem(userId, productId));
            } catch (DataIntegrityViolationException e) {
                // Concurrent toggle: another thread already inserted — treat as toggle-off
                wishlistRepository.findByUserIdAndProductId(userId, productId)
                        .ifPresent(wishlistRepository::delete);
            }
        }
    }

    // --- Notifications ---

    @Transactional(readOnly = true)
    public List<AppNotification> getNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    @Transactional(readOnly = true)
    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId, String userId) {
        AppNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));
        if (!notification.getUserId().equals(userId)) {
            throw new UnauthorizedException("Notification does not belong to user");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }
    
    @Transactional
    public void markAllAsRead(String userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Transactional
    public void createNotification(String userId, String title, String message, Long productId) {
        notificationRepository.save(new AppNotification(userId, title, message, productId));
    }
    
    @Transactional
    public void deleteNotification(Long notificationId, String userId) {
        log.info("Deleting notification with ID: {}", notificationId);
        AppNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));
        if (!notification.getUserId().equals(userId)) {
            throw new UnauthorizedException("Notification does not belong to user");
        }
        notificationRepository.delete(notification);
        log.info("Deleted notification {}", notificationId);
    }
    
    @Transactional
    public void deleteAllNotifications(String userId) {
        notificationRepository.deleteAllByUserId(userId);
    }
}
