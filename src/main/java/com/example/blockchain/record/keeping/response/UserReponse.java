package com.example.blockchain.record.keeping.response;


import com.example.blockchain.record.keeping.models.Permission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class UserReponse {
    private Long id;
    private String name;
    private String email;
    private List<String> permissions;
}
