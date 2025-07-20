package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.Student;
import com.example.blockchain.record.keeping.models.Wallet;
import com.example.blockchain.record.keeping.repositorys.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
@RequiredArgsConstructor
public class WalletService implements IWalletService{

    private final WalletRepository walletRepository;

    @Override
    public Wallet create(Wallet wallet) {
        return walletRepository.save(wallet);
    }

    @Override
    public Wallet update(Wallet wallet) {
        return walletRepository.save(wallet);
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

    @Override
    public boolean isWalletAddressValid(String walletAddress) {
        return walletRepository.existsByWalletAddress(walletAddress);
    }

    @Override
    public BigInteger getTotalCoin() {
        return walletRepository.getTotalCoin();
    }

    @Override
    public Wallet findByWalletAddress(String address) {
        return walletRepository.findByWalletAddress(address);
    }

    public void updateWalletCoinAmount(Wallet wallet, BigInteger coinDelta, boolean isIncrease) {
        if (wallet == null || coinDelta == null) {
            throw new IllegalArgumentException("Wallet hoặc số lượng coin không được null");
        }

        BigInteger currentBalance = wallet.getCoin();
        BigInteger newBalance;

        if (isIncrease) {
            newBalance = currentBalance.add(coinDelta);
        } else {
            newBalance = currentBalance.subtract(coinDelta);
        }

        wallet.setCoin(newBalance);
        walletRepository.save(wallet);
    }

}
