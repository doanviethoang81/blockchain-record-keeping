package com.example.blockchain.record.keeping.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DegreeClassificationByYearResponse {
    private int year;
    private Long pending;
    private Long approved;
    private Long rejected;
}
