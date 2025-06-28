package com.example.blockchain.record.keeping.dtos.request;

import lombok.Data;

@Data
public class TransactionDTO {
    private String hash;
    private String from;
    private String to;
    private String value;
    private String asset;
    private String blockNum;
    private String blockTimestamp;
    private String direction;
    private String gasPrice;    // đơn vị: wei
    private String gasUsed;     // đơn vị: gas
    private String transactionFee; // gasUsed * gasPrice (wei)
}
