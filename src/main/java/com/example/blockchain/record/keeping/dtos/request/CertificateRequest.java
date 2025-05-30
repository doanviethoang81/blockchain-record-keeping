package com.example.blockchain.record.keeping.dtos.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CertificateRequest {
    private String issueDate;
    private String diplomaNumber;
    private String grantor;
    private String signer;

    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate != null ? issueDate.trim() : null;
    }

    public void setDiplomaNumber(String diplomaNumber) {
        this.diplomaNumber = diplomaNumber != null ? diplomaNumber.trim() : null;
    }

    public void setGrantor(String grantor) {
        this.grantor = grantor != null ? grantor.trim() : null;
    }

    public void setSigner(String signer) {
        this.signer = signer != null ? signer.trim() : null;
    }
}
