package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.NotificationReceivers;
import com.example.blockchain.record.keeping.repositorys.NotificationReceiverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationReceiverService implements INotificationReceiverService{

    private final NotificationReceiverRepository notificationReceiverRepository;

    @Override
    public NotificationReceivers save(NotificationReceivers notificationReceivers) {
        return notificationReceiverRepository.save(notificationReceivers);
    }
}
