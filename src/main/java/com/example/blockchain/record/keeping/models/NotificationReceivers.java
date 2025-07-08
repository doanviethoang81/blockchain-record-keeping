package com.example.blockchain.record.keeping.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name ="notification_receivers")
public class NotificationReceivers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "notification_id")
    private Notifications Notification;

    @Column(name = "receiver_id")
    private Long receiverId;

    @Column(name = "is_read")
    private boolean isRead;
}
