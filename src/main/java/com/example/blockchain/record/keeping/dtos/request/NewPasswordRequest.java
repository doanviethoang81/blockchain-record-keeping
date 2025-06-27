package com.example.blockchain.record.keeping.dtos.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewPasswordRequest {
    private String email;
    private String newPassword;
    private String confirmPassword;
}
