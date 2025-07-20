package com.example.blockchain.record.keeping.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletSTUInfoDTO {
    private String stuCoin;
    private String stuCoinOfStudent;
}
