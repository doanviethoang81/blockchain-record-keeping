package com.example.blockchain.record.keeping.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Data
public class MonthlyCertificateStatisticsRequest {
    private Long month;
    private Long total;
}
