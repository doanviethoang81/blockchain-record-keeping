package com.example.blockchain.record.keeping.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FacultyDegreeStatisticRequest {
    private String departmentName;
    private Long degreePending;
    private Long degreeApproved;
    private Long degreeRejected;
    private Long certificatePending;
    private Long certificateApproved;
    private Long certificateRejected;
}
