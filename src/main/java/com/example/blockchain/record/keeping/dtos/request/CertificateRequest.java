package com.example.blockchain.record.keeping.dtos.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CertificateRequest {
    private String certificateTypeId;
    private LocalDate issueDate;
    private String diplomaNumber;

    public void setDiplomaNumber(String diplomaNumber) {
        this.diplomaNumber = diplomaNumber != null ? diplomaNumber.trim() : null;
    }
}
