package com.example.blockchain.record.keeping.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class StudentCoinResponse {
    private Long id;
    private String name;
    private String studentCode;
    private String email;
    private String className;
    private LocalDate birthDate;
    private String course;
    private String stuCoin;
    private String walletAddress;
}
