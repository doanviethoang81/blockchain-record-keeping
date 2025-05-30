package com.example.blockchain.record.keeping.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UniversityDetailResponse {
    private String name;
    private String email;
    private String address;
    private String taxCode;
    private String website;
    private String logo;
    private boolean isLocked;
    private boolean isVerifile;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
