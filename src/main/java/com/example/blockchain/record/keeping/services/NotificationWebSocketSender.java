package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.response.NotificationResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class NotificationWebSocketSender {
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationWebSocketSender(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendNotification(Long userId, NotificationResponse notification) {
        messagingTemplate.convertAndSend("/topic/notifications/" + userId, notification);
    }
}
