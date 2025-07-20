package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.Student;
import com.example.blockchain.record.keeping.models.Wallet;

import java.math.BigInteger;

public interface IWalletService {

    Wallet create(Wallet wallet);

    Wallet update(Wallet wallet );

    Wallet exchangeMoney(Long id);

    Wallet findById(Long id);

    Wallet findByStudent(Student student);

    boolean isWalletAddressValid(String walletAddress);

    BigInteger getTotalCoin();

    Wallet findByWalletAddress(String address);
}
