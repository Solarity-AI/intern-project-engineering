package com.solarityai.productreview.service.impl;

import com.solarityai.backendfw.exception.NotFoundException;
import com.solarityai.backendfw.query.model.PageRequestDto;
import com.solarityai.productreview.dto.AppNotificationDto;
import com.solarityai.productreview.dto.NotificationCreateDto;
import com.solarityai.productreview.dto.ProductDto;
import com.solarityai.productreview.dto.UnreadCountDto;
import com.solarityai.productreview.entity.AppNotificationEntity;
import com.solarityai.productreview.entity.ProductEntity;
import com.solarityai.productreview.entity.WishlistItemEntity;
import com.solarityai.productreview.mapper.NotificationMapper;
import com.solarityai.productreview.mapper.ProductMapper;
import com.solarityai.productreview.repository.AppNotificationRepository;
import com.solarityai.productreview.repository.ProductRepository;
import com.solarityai.productreview.repository.WishlistItemRepository;
import com.solarityai.productreview.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final WishlistItemRepository wishlistItemRepository;
    private final AppNotificationRepository notificationRepository;
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final NotificationMapper notificationMapper;

    @Override
    public List<UUID> getWishlistProductIds(String userId) {
        return wishlistItemRepository.findByUserId(userId).stream()
                .map(WishlistItemEntity::getProductId)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProductDto> getWishlistProducts(String userId, PageRequestDto pageRequest) {
        List<UUID> productIds = getWishlistProductIds(userId);

        if (productIds.isEmpty()) {
            return Page.empty();
        }

        Pageable pageable = createPageable(pageRequest);
        Page<ProductEntity> productPage = productRepository.findByIdIn(productIds, pageable);

        return productPage.map(productMapper::toDto);
    }

    @Override
    public void toggleWishlistItem(String userId, UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new NotFoundException("Product not found with id: " + productId);
        }

        Optional<WishlistItemEntity> existing = wishlistItemRepository.findByUserIdAndProductId(userId, productId);

        if (existing.isPresent()) {
            wishlistItemRepository.delete(existing.get());
        } else {
            WishlistItemEntity item = new WishlistItemEntity();
            item.setUserId(userId);
            item.setProductId(productId);
            wishlistItemRepository.save(item);
        }
    }

    @Override
    public List<AppNotificationDto> getNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByNotificationCreatedAtDesc(userId).stream()
                .map(notificationMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UnreadCountDto getUnreadNotificationCount(String userId) {
        Long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
        return new UnreadCountDto(count);
    }

    @Override
    public void markNotificationAsRead(UUID notificationId) {
        AppNotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification not found with id: " + notificationId));

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllNotificationsAsRead(String userId) {
        List<AppNotificationEntity> notifications = notificationRepository.findByUserId(userId);
        notifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }

    @Override
    public AppNotificationDto createNotification(String userId, NotificationCreateDto dto) {
        AppNotificationEntity notification = notificationMapper.toEntity(dto);
        notification.setUserId(userId);
        notification = notificationRepository.save(notification);

        return notificationMapper.toDto(notification);
    }

    @Override
    public void deleteNotification(UUID notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new NotFoundException("Notification not found with id: " + notificationId);
        }
        notificationRepository.deleteById(notificationId);
    }

    @Override
    public void deleteAllNotifications(String userId) {
        List<AppNotificationEntity> notifications = notificationRepository.findByUserId(userId);
        notificationRepository.deleteAll(notifications);
    }

    private Pageable createPageable(PageRequestDto pageRequest) {
        if (pageRequest == null) {
            return PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        }

        int page = pageRequest.getPage();
        int size = pageRequest.getSize() > 0 ? pageRequest.getSize() : 10;

        return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
    }
}
