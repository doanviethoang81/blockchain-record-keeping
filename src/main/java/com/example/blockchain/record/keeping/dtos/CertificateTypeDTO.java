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
public class CertificateTypeDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;
}
