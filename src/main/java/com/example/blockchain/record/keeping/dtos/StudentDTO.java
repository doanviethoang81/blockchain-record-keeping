package com.example.blockchain.record.keeping.dtos;

import com.example.blockchain.record.keeping.models.StudentClass;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
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
public class StudentDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("student_class_id")
    private Long studentClass;

    @JsonProperty("name")
    private String name;

    @JsonProperty("student_code")
    private String studentCode;

    @JsonProperty("email")
    private String email;

    @JsonProperty("birth_date")
    private LocalDate birthDate;

    @JsonProperty("course")
    private String course;

    @JsonProperty("status")
    private String status;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
