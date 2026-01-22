package com.productreview.backend.service.impl;

import com.productreview.backend.dto.ProductDTO;
import com.productreview.backend.entity.AppNotification;
import com.productreview.backend.entity.Product;
import com.productreview.backend.entity.WishlistItem;
import com.productreview.backend.exception.ResourceNotFoundException;
import com.productreview.backend.repository.NotificationRepository;
import com.productreview.backend.repository.ProductRepository;
import com.productreview.backend.repository.WishlistRepository;
import com.productreview.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public List<Long> getWishlist(String userId) {
        return wishlistRepository.findByUserId(userId).stream()
                .map(WishlistItem::getProductId)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProductDTO> getWishlistProducts(String userId, Pageable pageable) {
        List<Long> productIds = getWishlist(userId);

        if (productIds.isEmpty()) {
            return Page.empty(pageable);
        }

        Page<Product> productsPage = productRepository.findByIdIn(productIds, pageable);

        List<ProductDTO> productDTOs = productsPage.getContent().stream()
                .map(this::convertToProductDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(productDTOs, pageable, productsPage.getTotalElements());
    }

    @Override
    @Transactional
    public void toggleWishlist(String userId, Long productId) {
        Optional<WishlistItem> existingItem = wishlistRepository.findByUserIdAndProductId(userId, productId);

        if (existingItem.isPresent()) {
            wishlistRepository.delete(existingItem.get());
            log.info("Removed product {} from wishlist for user {}", productId, userId);
        } else {
            WishlistItem newItem = WishlistItem.builder()
                    .userId(userId)
                    .productId(productId)
                    .build();
            wishlistRepository.save(newItem);
            log.info("Added product {} to wishlist for user {}", productId, userId);
        }
    }

    @Override
    public List<AppNotification> getNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        AppNotification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(String userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Override
    @Transactional
    public void createNotification(String userId, String title, String message, Long productId) {
        AppNotification notification = AppNotification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .productId(productId)
                .read(false)
                .build();
        notificationRepository.save(notification);
        log.info("Created notification for user {}: {}", userId, title);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new ResourceNotFoundException("Notification not found with id: " + notificationId);
        }
        notificationRepository.deleteById(notificationId);
    }

    @Override
    @Transactional
    public void deleteAllNotifications(String userId) {
        notificationRepository.deleteByUserId(userId);
        log.info("Deleted all notifications for user {}", userId);
    }

    private ProductDTO convertToProductDTO(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .categories(product.getCategories())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .build();
    }
}
