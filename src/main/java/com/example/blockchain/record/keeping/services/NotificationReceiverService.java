package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.NotificationReceivers;
import com.example.blockchain.record.keeping.models.Notifications;
import com.example.blockchain.record.keeping.repositorys.NotificationReceiverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationReceiverService implements INotificationReceiverService{

    private final NotificationReceiverRepository notificationReceiverRepository;

    @Override
    public NotificationReceivers save(NotificationReceivers notificationReceivers) {
        return notificationReceiverRepository.save(notificationReceivers);
    }

    @Override
    public List<NotificationReceivers> saveAll(List<NotificationReceivers> list) {
        return notificationReceiverRepository.saveAll(list);
    }

    @Override
    public long countNotification(long userId, Boolean isRead, LocalDateTime startDate,LocalDateTime endDate) {
        return notificationReceiverRepository.countNotification(userId, isRead, startDate, endDate);
    }

    @Override
    public List<NotificationReceivers> notificationList(Long userId, Boolean isRead,LocalDateTime startDate,LocalDateTime endDate, int limit, int offset) {
        return notificationReceiverRepository.notificationList(userId,isRead, startDate, endDate, limit, offset);
    }

    @Override
    public NotificationReceivers isRead(NotificationReceivers notification) {
        notification.setRead(true);
        return notificationReceiverRepository.save(notification);
    }

    @Override
    public NotificationReceivers findById(Long id) {
        return notificationReceiverRepository.findById(id).orElse(null);
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        return notificationReceiverRepository.markAllAsRead(userId);
    }
}
