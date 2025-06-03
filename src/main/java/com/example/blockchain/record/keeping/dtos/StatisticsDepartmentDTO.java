package com.example.blockchain.record.keeping.dtos;

public interface StatisticsDepartmentDTO {
    Long getClassCount();
    Long getStudentCount();
    Long getCertificatePending();
    Long getCertificateApproved();
    Long getCertificateRejected();
    Long getDegreePending();
    Long getDegreeApproved();
    Long getDegreeRejected();
}
