package com.example.blockchain.record.keeping.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name ="student_id")
    private Student student;

    @Column(name = "wallet_address ")
    private String walletAddress ;

    @Column(name = "coin ")
    private BigInteger coin ;

    @Column(name = "private_key")
    private String privateKey;

    @Column(name = "public_key")
    private String publicKey;
}
