package com.example.blockchain.record.keeping.dtos;

public interface StatisticsAdminDTO {
    Long getUniversityCount();
    Long getStudentCount();
    Long getCertificatePending();
    Long getCertificateApproved();
    Long getCertificateRejected();
    Long getDegreesPending();
    Long getDegreesApproved();
    Long getDegreesRejected();
}
