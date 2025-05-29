package com.example.blockchain.record.keeping.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
public class CertificateDetailReponse {
    private Long id;
    private String nameStudent;
    private String studentClass;
    private String department;
    private String university;
    private String certificateName;
    private LocalDate issueDate;
    private String diploma_number;
    private String studentCode;
    private String email;
    private LocalDate birthDate;
    private String course;
    private String image_url;
    private String qrCodeUrl;
    private LocalDateTime createdAt;
}
