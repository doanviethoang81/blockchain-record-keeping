package com.example.blockchain.record.keeping.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Getter
@Setter
@Table(name = "university_certificate_types")
public class UniversityCertificateType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name ="university_id")
    private University university;

    @ManyToOne
    @JoinColumn(name ="certificate_type_id")
    private CertificateType certificateType;
}
