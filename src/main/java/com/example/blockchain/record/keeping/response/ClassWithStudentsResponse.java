package com.example.blockchain.record.keeping.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ClassWithStudentsResponse {
    private String className;
    private List<StudentDetailReponse> studentDetail;
}
