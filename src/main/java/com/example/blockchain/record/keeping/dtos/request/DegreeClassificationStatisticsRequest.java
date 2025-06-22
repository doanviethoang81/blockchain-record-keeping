package com.example.blockchain.record.keeping.dtos.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DegreeClassificationStatisticsRequest {
    private Long excellent;
    private Long veryGood;
    private Long good;
    private Long average;
}
