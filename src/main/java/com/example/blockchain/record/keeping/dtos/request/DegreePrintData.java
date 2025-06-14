package com.example.blockchain.record.keeping.dtos.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DegreePrintData {
    private String universityName;
    private String degreeTitle;
    private String departmentName;
    private String name;
    private String birthDate;
    private String graduationYear;
    private String rating;
    private String educationMode;
    private String day;
    private String month;
    private String year;
    private String trainingLocation;
    private String signer;
    private String diplomaNumber;
    private String lotteryNumber;
}
