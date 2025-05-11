package com.example.blockchain.record.keeping.dtos.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CertificateTypeRequest {
    private String name;

    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }
}
