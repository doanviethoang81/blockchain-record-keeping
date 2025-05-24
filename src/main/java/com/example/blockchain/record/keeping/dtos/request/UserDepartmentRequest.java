package com.example.blockchain.record.keeping.dtos.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDepartmentRequest {
    private String name;
    private String email;
    private String password;

    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim() : null;
    }

    public void setPassword(String password) {
        this.password = password != null ? password.trim() : null;
    }
}
