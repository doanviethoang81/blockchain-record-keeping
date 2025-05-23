package com.example.blockchain.record.keeping.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DegreeDTO {

    @JsonProperty("issue_date")
    private LocalDate issueDate;

    @JsonProperty("graduation_year")
    private String graduationYear;

    @JsonProperty("education_mode")
    private String educationMode;

    @JsonProperty("training_location")
    private String trainingLocation;

    @JsonProperty("signer")
    private String signer;

    @JsonProperty("diploma_number")
    private String diplomaNumber;

    @JsonProperty("lottery_number")
    private String lotteryNumber;

    @JsonProperty("blockchain_tx_hash")
    private String blockchainTxHash;

    @JsonProperty("rating")
    private String rating; // xếp loại

    @JsonProperty("degree_title")
    private String degreeTitle;// loại bằng

    private String imageUrl;

    @JsonProperty("created_at ")
    private LocalDateTime createdAt ;

}
