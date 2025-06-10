package com.example.blockchain.record.keeping.dtos.request;

import lombok.Data;

@Data
public class DecryptRequest {
    private String transactionHash;
    private String publicKeyBase64;
}
