package com.example.blockchain.record.keeping.response;


import com.example.blockchain.record.keeping.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponse {
    private Long id;
    private String title;
    private String content;
    private String rejectedNote;
    private NotificationType type;
    private boolean isRead;
    private String documentType;
    private Long documentId;
    private LocalDateTime createdAt;
}
