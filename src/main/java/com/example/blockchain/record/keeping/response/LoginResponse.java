package com.example.blockchain.record.keeping.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
    private String fullname;
    private String email;
    private String token;
    private String role;
    private String redirectUrl;
    private List<String> authorities;
}
