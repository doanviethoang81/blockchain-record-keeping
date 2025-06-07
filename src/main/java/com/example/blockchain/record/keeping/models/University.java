package com.example.blockchain.record.keeping.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Getter
@Setter
@Table(name = "universitys")
public class University {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name ="name")
    private String name;

    @Column(name ="address")
    private String address;

    @Column(name ="email")
    private String email;

    @Column(name ="tax_code")
    private String taxCode;

    @Column(name ="website")
    private String website;

    @Column(name ="logo")
    private String logo;

    @Column(name ="seal_image_url")
    private String sealImageUrl;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Column(name="updated_at")
    private LocalDateTime updatedAt;
}

