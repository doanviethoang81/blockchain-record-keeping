package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.Notifications;
import com.example.blockchain.record.keeping.repositorys.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificateService implements INotificateService{

    private final NotificationRepository notificationRepository;

    @Override
    public Notifications save(Notifications notifications) {
        return notificationRepository.save(notifications);
    }
}
