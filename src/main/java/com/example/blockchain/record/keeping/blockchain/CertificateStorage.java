package com.example.blockchain.record.keeping.blockchain;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.tx.Contract;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.abi.datatypes.Utf8String;

import java.util.Arrays;
import java.util.Collections;

public class CertificateStorage extends Contract {
    private static final String BINARY = "";

    public CertificateStorage(String contractAddress, Web3j web3j, Credentials credentials) {
        super(BINARY, contractAddress, web3j, credentials, new DefaultGasProvider());
    }

    public TransactionReceipt storeCertificate(String studentCode, String diplomaNumber, String degreeTitle) throws Exception {
        try {
            return executeRemoteCallTransaction(
                    new org.web3j.abi.datatypes.Function(
                            "storeCertificate",
                            Arrays.asList(new Utf8String(studentCode), new Utf8String(diplomaNumber), new Utf8String(degreeTitle)),
                            Collections.emptyList()
                    )
            ).send();
        } catch (Exception e) {
            System.err.println("LỖI khi gọi smart contract: " + e.getMessage());
            throw e;
        }

    }
}
