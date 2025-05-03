package com.example.blockchain.record.keeping.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@Data
public class RegisterRequest {

    @NotBlank(message = "Tên trường không được để trống!")
    @JsonProperty("name")
    private String name;

    @NotBlank(message = "Địa chỉ không được để trống!")
    @JsonProperty("address")
    private String address;

    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được để trống!")
    @JsonProperty("email")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống!")
    @JsonProperty("tax_code")
    private String taxCode;

    @NotBlank(message = "Tên website không được để trống!")
    @JsonProperty("website")
    private String website;

    @NotBlank(message = "Logo không được để trống!")
    @JsonProperty("logo")
    private String logo;

    @NotBlank(message = "Mật khẩu không được để trống!")
    @JsonProperty("password")
    private String password;
}
