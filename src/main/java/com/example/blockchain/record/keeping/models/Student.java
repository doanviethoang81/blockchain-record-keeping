package com.example.blockchain.record.keeping.models;

import com.example.blockchain.record.keeping.enums.Status;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    @JoinColumn(name ="student_class_id")
    private StudentClass studentClass;

    @Column(name ="name")
    private String name;

    @Column(name ="student_code")
    private String studentCode;

    @Column(name ="email")
    private String email;

    @Column(name ="birth_date")
    private LocalDate birthDate;

    @Column(name ="course")
    private String course;

    @Column(name ="status")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name ="password")
    private String password;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;
}
