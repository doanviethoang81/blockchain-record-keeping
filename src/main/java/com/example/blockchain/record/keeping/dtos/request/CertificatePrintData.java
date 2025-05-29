package com.example.blockchain.record.keeping.dtos.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CertificatePrintData {
    private String universityName;
    private String certificateTitle;
    private String studentName;
    private String departmentName;
    private String certificateName;
    private String diplomaNumber;
    private String issueDate;
    private String grantor;
    private String signer;
}
