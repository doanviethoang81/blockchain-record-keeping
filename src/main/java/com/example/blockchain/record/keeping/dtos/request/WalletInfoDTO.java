package com.example.blockchain.record.keeping.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletInfoDTO {
    private String address;
    private String balanceEth;     // Số dư ETH
    private String nonce;          // Số lần gửi tx
    private String gasPriceGwei;   // Gas price hiện tại
}
