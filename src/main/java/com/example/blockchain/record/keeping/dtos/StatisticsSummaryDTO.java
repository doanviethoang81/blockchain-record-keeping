package com.example.blockchain.record.keeping.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class StatisticsSummaryDTO {
    private Map<String, Object> topDepartmentCertificates;
    private Map<String, Object> topDepartmentDegrees;
    private Map<String, Object> topClassCertificates;
    private Map<String, Object> topClassDegrees;
    private Map<String, Object> getTopCertificateType;
}
