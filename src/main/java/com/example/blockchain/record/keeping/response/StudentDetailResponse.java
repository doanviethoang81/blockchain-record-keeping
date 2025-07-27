package com.example.blockchain.record.keeping.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
public class StudentDetailResponse {
    private String name;
    private String studentCode;
    private String email;
    private LocalDate birthDate;
    private String course;
    private Long classId;
    private String className;
    private Long departmentId;
    private String departmentName;
    private String universityName;
    private String walletAddress;
    private String publicKey;
    private String privateKey;
    private String coin;
}
