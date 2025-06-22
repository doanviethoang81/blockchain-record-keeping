package com.example.blockchain.record.keeping.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class FacultyDegreeStatisticResponse {
    private String departmentName;
    private Long degreePending;
    private Long degreeApproved;
    private Long degreeRejected;
    private Long certificatePending;
    private Long certificateApproved;
    private Long certificateRejected;
}
