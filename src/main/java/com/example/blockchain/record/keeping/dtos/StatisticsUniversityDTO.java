package com.example.blockchain.record.keeping.dtos;

public interface StatisticsUniversityDTO {
    Long getDepartmentCount();
    Long getClassCount();
    Long getStudentCount();
    Long getCertificatePending();
    Long getCertificateApproved();
    Long getCertificateRejected();
    Long getDegreePending();
    Long getDegreeApproved();
    Long getDegreeRejected();
}
