package com.example.blockchain.record.keeping.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table( name ="certificates")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "university_certificate_type_id")
    private UniversityCertificateType universityCertificateType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(name = "issue_date")
    private LocalDate issueDate;

    @Column(name = "graduation_year")
    private String graduationYear;

    @Column(name = "education_mode")
    private String educationMode;

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

    @Column(name ="rating")
    private String rating; // xếp loại

    @Column(name ="degree_title")
    private String degreeTitle;// loại bằng cử nhân ...

    @Column(name = "status")
    private String status;

    @Column(name = "image_url")
    private String imageUrl;
    //thêm url image
}
