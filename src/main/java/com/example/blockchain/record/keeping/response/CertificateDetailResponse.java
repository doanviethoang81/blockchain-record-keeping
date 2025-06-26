package com.example.blockchain.record.keeping.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
public class CertificateDetailResponse {
    private Long id;
    private Long studentId;
    private String nameStudent;
    private String studentClass;
    private String department;
    private String university;
    private Long certificateTypeId;
    private String certificateName;
    private LocalDate issueDate;
    private String diplomaNumber;
    private String studentCode;
    private String email;
    private LocalDate birthDate;
    private String course;
    private String grantor;
    private String signer;
    private String status;
    private String image_url;
    private String ipfsUrl;
    private String qrCodeUrl;
    private String transactionHash;
    private LocalDateTime createdAt;
}
