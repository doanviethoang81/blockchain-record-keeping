package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.Student;
import com.example.blockchain.record.keeping.models.Wallet;
import com.example.blockchain.record.keeping.repositorys.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletService implements IWalletService{

    private final WalletRepository walletRepository;

    @Override
    public Wallet create(Wallet wallet) {
        return walletRepository.save(wallet);
    }

    @Override
    public Wallet update(Long id) {
        return null;
    }

    @Override
    public Wallet exchangeMoney(Long id) {
        return null;
    }

    @Override
    public Wallet findById(Long id) {
        return walletRepository.findById(id).orElseThrow(null);
    }

    @Override
    public Wallet findByStudent(Student student) {
        return walletRepository.findByStudent(student).orElse(null);
    }
}
