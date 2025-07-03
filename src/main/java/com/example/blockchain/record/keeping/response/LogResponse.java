package com.example.blockchain.record.keeping.response;

import com.example.blockchain.record.keeping.enums.ActionType;
import com.example.blockchain.record.keeping.enums.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
public class LogResponse {
    private Long id;
    private Long userId;
    private ActionType actionType;
    private Entity entityName;
    private Long entityId;
    private String description;
    private String ipAddress;
    private LocalDateTime createdAt;
    private List<ActionChangeResponse> actionChange;
}
