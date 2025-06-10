package com.example.blockchain.record.keeping.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentDetailResponse {
    private String nameDepartment;
    private String email;
    private UniversityResponse universityResponse;
}
