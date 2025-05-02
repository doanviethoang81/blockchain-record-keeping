package com.example.blockchain.record.keeping.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentDTO {

    @JsonProperty("department_id")
    private String department;

    @JsonProperty("name")
    private String name;

    @JsonProperty("student_code")
    private String studentCode;

    @JsonProperty("email")
    private String email;

    @JsonProperty("class_name")
    private String className;

    @JsonProperty("birth_date")
    private LocalDate birthDate;

    @JsonProperty("faculty")
    private String faculty;

    @JsonProperty("course")
    private String course;
}
