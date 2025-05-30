package com.example.blockchain.record.keeping.models;

import com.example.blockchain.record.keeping.enums.Status;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Column(name = "diploma_number")
    private String diplomaNumber;

    @Column(name = "signer")
    private String signer;

    @Column(name = "grantor")
    private String grantor;

    @Column(name = "blockchain_tx_hash")
    private String blockchainTxHash;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "qr_code_url")
    private String qrCodeUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;
}
