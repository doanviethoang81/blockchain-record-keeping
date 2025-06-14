package com.example.blockchain.record.keeping.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DegreeRequest {
    private Long studentId;
    private Long ratingId;
    private Long degreeTitleId;
    private Long educationModeId;
    private String issueDate;
    private String graduationYear;
    private String trainingLocation;
    private String signer;
    private String diplomaNumber;
    private String lotteryNumber;

    public void setTrainingLocation(String trainingLocation) {
        this.trainingLocation = trainingLocation != null ? trainingLocation.trim() : null;
    }

    public void setSigner(String signer) {
        this.signer = signer != null ? signer.trim() : null;
    }

    public void setDiplomaNumber(String diplomaNumber) {
        this.diplomaNumber = diplomaNumber != null ? diplomaNumber.trim() : null;
    }

    public void setLotteryNumber(String lotteryNumber) {
        this.lotteryNumber = lotteryNumber != null ? lotteryNumber.trim() : null;
    }
}
