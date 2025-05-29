package com.example.blockchain.record.keeping.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class CertificateReponse {
    private Long id;
    private String nameStudent;
    private LocalDate issueDate;
    private String diploma_number;
    private String certificateName;
    private LocalDateTime createdAt;
}
