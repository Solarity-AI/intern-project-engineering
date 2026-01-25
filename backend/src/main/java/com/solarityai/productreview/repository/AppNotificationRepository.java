package com.solarityai.productreview.repository;

import com.solarityai.backendfw.foundation.repository.BaseRepository;
import com.solarityai.productreview.entity.AppNotificationEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AppNotificationRepository extends BaseRepository<AppNotificationEntity, UUID> {

    List<AppNotificationEntity> findByUserIdOrderByNotificationCreatedAtDesc(String userId);

    Long countByUserIdAndIsReadFalse(String userId);

    List<AppNotificationEntity> findByUserId(String userId);
}
