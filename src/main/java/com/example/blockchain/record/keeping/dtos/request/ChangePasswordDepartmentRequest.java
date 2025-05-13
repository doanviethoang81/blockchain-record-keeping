package com.example.blockchain.record.keeping.dtos.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;

@Getter
@Setter
public class ChangePasswordDepartmentRequest {
    private Long id;
    private String newPassword;

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword != null ? newPassword.trim() :null;
    }
}
