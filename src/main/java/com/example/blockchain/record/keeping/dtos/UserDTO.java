package com.example.blockchain.record.keeping.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDTO {

    @JsonProperty("role_id")
    private Long roleId;

    @JsonProperty("university_id")
    private Long universityId;

    @JsonProperty("password")
    private String password;

    @JsonProperty("email")
    private String email;
}
