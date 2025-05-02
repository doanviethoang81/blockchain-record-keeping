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
public class UniversityCertificateTypeDTO {
    @JsonProperty("university_id")
    private Long universityId;

    @JsonProperty("certificate_type_id")
    private String certificateTypeId;
}
