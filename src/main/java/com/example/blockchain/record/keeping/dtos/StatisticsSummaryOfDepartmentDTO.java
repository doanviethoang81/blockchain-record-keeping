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
public class StatisticsSummaryOfDepartmentDTO {
    private Map<String, Object> topClassCertificates;
    private Map<String, Object> topClassDegrees;
    private Map<String, Object> getTopCertificateType;
}
