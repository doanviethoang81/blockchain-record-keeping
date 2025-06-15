package com.example.blockchain.record.keeping.services;

import com.certificate.contract.CertificateStorage_sol_EncryptedCertificateStorage;
import com.example.blockchain.record.keeping.utils.EnvUtil;
import com.example.blockchain.record.keeping.utils.RSAUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.utils.Numeric;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BlockChainService {
    private final Web3j web3j;
    private final Credentials credentials;
    private final ContractGasProvider gasProvider;
    private final CertificateStorage_sol_EncryptedCertificateStorage contract;

    @Autowired
    public BlockChainService(Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        this.web3j = web3j;
        this.credentials = credentials;
        this.gasProvider = gasProvider;

        String contractAddress = EnvUtil.get("SMART_CONTRACT_CERTIFICATE_ADDRESS");
        this.contract = CertificateStorage_sol_EncryptedCertificateStorage.load(
                contractAddress,
                web3j,
                credentials,
                gasProvider
        );
    }

    public String issue(String encryptedData) throws Exception {
        try {
            TransactionReceipt receipt = contract.saveCertificate(encryptedData).send();
            return receipt.getTransactionHash();
        } catch (Exception e) {
            throw new Exception("Transaction failed: " + e.getMessage());
        }
    }

    public String extractEncryptedData(String transactionHash) throws Exception {
        EthTransaction transactionResponse = web3j.ethGetTransactionByHash(transactionHash).send();
        Optional<Transaction> txOpt = transactionResponse.getTransaction();

        if (txOpt.isEmpty()) {
            throw new RuntimeException("Transaction không tồn tại.");
        }

        String input = txOpt.get().getInput();
        if (input == null || input.length() <= 10) {
            throw new RuntimeException("Không tìm thấy input data trong transaction.");
        }

        // Bỏ 4 byte đầu (8 ký tự hex) + "0x"
        String encodedHex = input.substring(10);
        byte[] decodedBytes = Numeric.hexStringToByteArray(encodedHex);
        return new String(decodedBytes, StandardCharsets.UTF_8).trim();
    }
}
