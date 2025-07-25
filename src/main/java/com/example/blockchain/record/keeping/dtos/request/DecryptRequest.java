package com.example.blockchain.record.keeping.dtos.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DecryptRequest {
    private String transactionHash;
    private String publicKeyBase64;
}
