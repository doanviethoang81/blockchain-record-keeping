package com.example.blockchain.record.keeping.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@AllArgsConstructor
@Getter
@Setter
public class FacultyDegreeStatisticResponse {
    private String departmentName;
    private Long validatedCount;
    private Long notValidatedCount;
}
