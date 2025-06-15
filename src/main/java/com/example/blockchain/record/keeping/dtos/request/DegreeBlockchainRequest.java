package com.example.blockchain.record.keeping.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DegreeBlockchainRequest {
    private String studentName;
    private String university;
    private String createdAt;
    private String diplomaNumber;
    private String lotteryNumber;
    private String ipfsUrl;
}
