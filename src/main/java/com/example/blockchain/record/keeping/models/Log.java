package com.example.blockchain.record.keeping.models;

import com.example.blockchain.record.keeping.enums.ActionType;
import com.example.blockchain.record.keeping.enums.Status;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name ="logs")
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name ="action_type")
    private ActionType actionType;

    @Enumerated(EnumType.STRING)
    @Column(name ="entity_name")
    private com.example.blockchain.record.keeping.enums.Entity entityName;

    @Column(name ="entity_id")
    private Long entityId;

    @Column(name ="description")
    private String description;

    @Column(name ="ip_address")
    private String ipAddress;

    @Column(name="created_at")
    private LocalDateTime createdAt;
}
