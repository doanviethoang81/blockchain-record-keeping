package com.example.blockchain.record.keeping.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CertificateLogDTO {

    @JsonProperty("certificate_id")
    private String certificateId;

    @JsonProperty("action")
    private String action;

    @JsonProperty("created_at")
    private String createdAt;

}
