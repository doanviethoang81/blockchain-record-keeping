package com.example.blockchain.record.keeping.response;

import com.example.blockchain.record.keeping.dtos.CertificateDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class StudentWithCertificateResponse {

    private String university;
    private String department;
    private String name;
    private String studentCode;
    private String email;
    private String className;
    private LocalDate birthDate;
    private String course;
    private List<CertificateDTO> certificates;
}
