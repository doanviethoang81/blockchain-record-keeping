package com.example.blockchain.record.keeping.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class DegreeResponse {
    private Long id;
    private String nameStudent;
    private String className;
    private String department;
    private LocalDate issueDate;
    private String status;
    private String graduationYear;
    private String diplomaNumber;
    private LocalDateTime createdAt;
}
