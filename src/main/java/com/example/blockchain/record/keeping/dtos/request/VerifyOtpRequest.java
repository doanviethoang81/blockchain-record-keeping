package com.example.blockchain.record.keeping.dtos.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpRequest {//DTO nhận dữ liệu từ client (FE gửi lên)
    private String email;
    private String otp;
}
