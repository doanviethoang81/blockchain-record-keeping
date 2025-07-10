package com.example.blockchain.record.keeping.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UniversityRequest {
    @NotBlank(message = "Tên trường không được để trống")
    private String name;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    @NotBlank(message = "Mã số thuế không được để trống")
    private String taxCode;

    @NotBlank(message = "Website không được để trống")
    private String website;
}
