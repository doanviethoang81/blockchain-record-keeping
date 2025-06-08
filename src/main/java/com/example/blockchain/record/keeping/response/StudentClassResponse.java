package com.example.blockchain.record.keeping.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class StudentClassResponse {
    private Long id;
    private String className;
}
