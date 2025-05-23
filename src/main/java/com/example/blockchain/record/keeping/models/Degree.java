package com.example.blockchain.record.keeping.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name ="degrees")
public class Degree {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "rating_id")
    private Rating rating;

    @ManyToOne
    @JoinColumn(name = "degree_title_id")
    private DegreeTitle degreeTitle;

    @ManyToOne
    @JoinColumn(name = "education_mode_id")
    private EducationMode educationMode;

    @Column(name = "graduation_year")
    private String graduationYear;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "training_location")
    private String trainingLocation;

    @Column(name = "signer")
    private String signer;

    @Column(name = "diploma_number")
    private String diplomaNumber;

    @Column(name = "lottery_number")
    private String lotteryNumber;

    @Column(name = "blockchain_tx_hash")
    private String blockchainTxHash;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "status")
    private String status;

    @Column(name="created_at")
    private LocalDateTime createdAt;
}
