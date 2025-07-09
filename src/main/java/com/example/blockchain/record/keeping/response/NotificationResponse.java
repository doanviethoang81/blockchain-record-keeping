package com.example.blockchain.record.keeping.response;


import com.example.blockchain.record.keeping.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {

    private Long id;
    private String title;
    private String content;
    private NotificationType type;
    private boolean isRead;
    private LocalDateTime createdAt;
}
