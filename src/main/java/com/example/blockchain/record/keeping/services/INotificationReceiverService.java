package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.NotificationReceivers;

import java.time.LocalDateTime;
import java.util.List;

public interface INotificationReceiverService {

    NotificationReceivers save(NotificationReceivers notificationReceivers);

    List<NotificationReceivers> saveAll(List<NotificationReceivers> list);

    long countNotification(long userId,Boolean isRead,LocalDateTime startDate, LocalDateTime endDate);

    List<NotificationReceivers> notificationList(Long userId, Boolean isRead, LocalDateTime startDate, LocalDateTime endDate, int limit, int offset);
}
