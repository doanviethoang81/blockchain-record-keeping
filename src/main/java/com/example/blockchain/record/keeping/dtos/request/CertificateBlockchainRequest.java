package com.example.blockchain.record.keeping.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class CertificateBlockchainRequest {
    private String studentName;
    private String university;
    private String createdAt;
    private String diplomaNumber;
    private String ipfsUrl;

}
