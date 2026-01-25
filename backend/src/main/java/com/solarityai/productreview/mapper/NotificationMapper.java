package com.solarityai.productreview.mapper;

import com.solarityai.productreview.dto.AppNotificationDto;
import com.solarityai.productreview.dto.NotificationCreateDto;
import com.solarityai.productreview.entity.AppNotificationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    AppNotificationDto toDto(AppNotificationEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "isRead", constant = "false")
    @Mapping(target = "notificationCreatedAt", ignore = true)
    AppNotificationEntity toEntity(NotificationCreateDto dto);
}
