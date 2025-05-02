package com.example.blockchain.record.keeping.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlockchainRecordDTO {

    @JsonProperty("certificate_id")
    private String certificateId;

    @JsonProperty("blockchain_tx_hash")
    private String blockchainTxHash;

    @JsonProperty("blockchain_network")
    private String blockchainNetwork;

    @JsonProperty("created_at")
    private String createdAt;

}
