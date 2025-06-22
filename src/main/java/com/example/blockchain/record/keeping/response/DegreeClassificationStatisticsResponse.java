package com.example.blockchain.record.keeping.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DegreeClassificationStatisticsResponse {
    private Long excellent;
    private Long veryGood;
    private Long good;
    private Long average;

}
