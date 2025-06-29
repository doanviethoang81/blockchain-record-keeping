package com.example.blockchain.record.keeping.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
public class CertificateOfStudentResponse {
    private Long id;
    private String nameStudent;
    private String className;
    private String department;
    private LocalDate issueDate;
    private String status;
    private String diplomaNumber;
    private String certificateName;
    private LocalDateTime createdAt;
}
