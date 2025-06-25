package com.example.blockchain.record.keeping.response;

import com.example.blockchain.record.keeping.enums.Status;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class DegreeDetailResponse {
    private Long id;
    private String nameStudent;
    private String className;
    private String departmentName;
    private String university;
    private String studentCode;
    private LocalDate issueDate;
    private String graduationYear;
    private String email;
    private LocalDate birthDate;
    private Long ratingId;
    private String ratingName;
    private Long degreeTitleId;
    private String degreeTitleName;
    private Long educationModeId;
    private String educationModeName;
    private String course;
    private String signer;
    private Status status;
    private String imageUrl;
    private String ipfsUrl;
    private String qrCodeUrl;
    private String transactionHash;
    private String diplomaNumber;
    private String lotteryNumber;
    private LocalDateTime createdAt;
}
