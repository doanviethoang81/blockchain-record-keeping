package com.example.blockchain.record.keeping.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CountCertificateTypeRequest {
    private String name;
    private Long approved;
}
