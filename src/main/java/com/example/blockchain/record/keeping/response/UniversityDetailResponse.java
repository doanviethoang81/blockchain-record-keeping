package com.example.blockchain.record.keeping.response;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UniversityDetailResponse {
    private String name;
    private String email;
    private String address;
    private String taxCode;
    private String website;
    private String logo;
}
