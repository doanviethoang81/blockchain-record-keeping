package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.NotificationReceivers;

public interface INotificationReceiverService {

    NotificationReceivers save(NotificationReceivers notificationReceivers);
}
