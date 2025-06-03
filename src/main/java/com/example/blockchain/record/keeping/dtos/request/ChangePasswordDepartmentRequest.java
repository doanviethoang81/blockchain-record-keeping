package com.example.blockchain.record.keeping.dtos.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.GetMapping;

@Getter
@Setter
public class ChangePasswordDepartmentRequest {
    private String passwordUniversity;
    private String newPassword;
    private String confirmPassword;


    public void setPasswordUniversity(String passwordUniversity) {
        this.passwordUniversity = passwordUniversity != null ? passwordUniversity.trim() :null;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword != null ? newPassword.trim() :null;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword != null ? confirmPassword.trim() :null;
    }
}
