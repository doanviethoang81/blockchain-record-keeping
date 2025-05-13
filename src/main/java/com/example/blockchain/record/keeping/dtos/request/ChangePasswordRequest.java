package com.example.blockchain.record.keeping.dtos.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangePasswordRequest {
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword != null ? oldPassword.trim() : null;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword!= null ? newPassword.trim() : null;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword!= null ? confirmPassword.trim() : null;
    }
}
