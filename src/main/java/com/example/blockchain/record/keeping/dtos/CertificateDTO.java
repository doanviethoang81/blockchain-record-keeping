package com.example.blockchain.record.keeping.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CertificateDTO {

    @JsonProperty("student_id")
    private Long studentId;

    @JsonProperty("university_certificate_type_id")
    private Long universityCertificateTypeId;

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

    @JsonProperty("status")
    private String status;

    @JsonProperty("rating")
    private String rating; // xếp loại

    @JsonProperty("degree_title")
    private String degreeTitle;// loại bằng
}
