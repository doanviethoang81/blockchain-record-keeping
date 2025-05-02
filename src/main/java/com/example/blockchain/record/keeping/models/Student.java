package com.example.blockchain.record.keeping.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name ="department_id")
    private Department department;

    @Column(name ="name")
    private String name;

    @Column(name ="student_code")
    private String studentCode;

    @Column(name ="email")
    private String email;

    @Column(name ="class_name")
    private String className;

    @Column(name ="birth_date")
    private LocalDate birthDate;

    @Column(name ="course")
    private String course;
}
