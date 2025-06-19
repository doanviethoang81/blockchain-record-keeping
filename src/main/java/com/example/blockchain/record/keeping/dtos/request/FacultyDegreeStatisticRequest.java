package com.example.blockchain.record.keeping.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FacultyDegreeStatisticRequest {
    private String departmentName;
    private Long validatedDegreeCount;
    private Long notValidatedDegreeCount;
    private Long validatedCertificateCount;
    private Long notValidatedCertificateCount;
}
