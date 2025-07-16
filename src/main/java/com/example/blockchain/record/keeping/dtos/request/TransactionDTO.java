package com.example.blockchain.record.keeping.dtos.request;

import lombok.Data;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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

    public void setBlockTimestampFromUnix(String unixTimestamp) {
        try {
            long timestampSeconds = Long.parseLong(unixTimestamp);
            Instant instant = Instant.ofEpochSecond(timestampSeconds);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    .withZone(ZoneId.of("UTC"));
            this.blockTimestamp = formatter.format(instant);
        } catch (Exception e) {
            this.blockTimestamp = "Invalid timestamp";
        }
    }

}
