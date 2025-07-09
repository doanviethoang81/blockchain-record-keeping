package com.example.blockchain.record.keeping.dtos.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UniversityRequest {
    private String name;
    private String email;
    private String address;
    private String taxCode;
    private String website;
    private MultipartFile logo;
    private MultipartFile sealImageUrl;
}
