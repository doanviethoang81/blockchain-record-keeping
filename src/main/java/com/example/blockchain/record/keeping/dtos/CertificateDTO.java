package com.example.blockchain.record.keeping.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CertificateDTO {

    @JsonProperty("university_certificate_type_id")
    private String universityCertificateType;

    @JsonProperty("issue_date")
    private LocalDate issueDate;

    @JsonProperty("diploma_number")
    private String diplomaNumber;

    @JsonProperty("blockchain_tx_hash")
    private String blockchainTxHash;

    @JsonProperty("imageUrl")
    private String imageUrl;

    @JsonProperty("qrCode")
    private String qrCode;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

}
