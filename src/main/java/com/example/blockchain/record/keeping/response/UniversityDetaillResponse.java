package com.example.blockchain.record.keeping.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UniversityDetaillResponse {
    private String name;
    private String email;
    private String address;
    private String taxCode;
    private String website;
    private String publicKey;
    private String privateKey;
    private String logo;
    private String sealImageUrl;
}
