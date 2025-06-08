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
    private String className;
    private String departmentName;
    private String universityName;
}
