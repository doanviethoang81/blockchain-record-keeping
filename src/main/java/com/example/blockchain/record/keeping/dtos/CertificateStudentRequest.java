package com.example.blockchain.record.keeping.dtos;

import com.example.blockchain.record.keeping.models.Certificate;
import com.example.blockchain.record.keeping.models.Student;
import lombok.Data;

@Data
public class CertificateStudentRequest {
    private CertificateDTO certificate;
    private StudentDTO student;
}
