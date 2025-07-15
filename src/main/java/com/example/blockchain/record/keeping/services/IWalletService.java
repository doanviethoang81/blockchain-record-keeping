package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.Student;
import com.example.blockchain.record.keeping.models.Wallet;

public interface IWalletService {

    Wallet create(Wallet wallet);

    Wallet update(Long id);

    Wallet exchangeMoney(Long id);

    Wallet findById(Long id);

    Wallet findByStudent(Student student);
}
