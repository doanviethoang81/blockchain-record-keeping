package com.example.blockchain.record.keeping.services;

import com.STUcoin.contract.STUcoin_sol_STUcoin;
import com.example.blockchain.record.keeping.utils.EnvUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;
import org.web3j.utils.Convert;

import java.math.BigInteger;

@Service
@RequiredArgsConstructor
public class STUcoinService {

    private final STUcoin_sol_STUcoin contract;
    BigInteger gasPrice = Convert.toWei("20", Convert.Unit.GWEI).toBigInteger();
    BigInteger gasLimit = BigInteger.valueOf(200_000);
    ContractGasProvider gasProvider = new StaticGasProvider(gasPrice, gasLimit);

    @Autowired
    public STUcoinService() {
        Web3j web3j = Web3j.build(new HttpService(EnvUtil.get("ALCHEMY_URL")));
        Credentials credentials = Credentials.create(EnvUtil.get("METAMASK_PRIVATE_KEY"));
        String contractAddress = EnvUtil.get("SMART_CONTRACT_STUCOIN_ADDRESS");

        this.contract = STUcoin_sol_STUcoin.load(
                contractAddress,
                web3j,
                new RawTransactionManager(web3j, credentials),
                gasProvider
        );
    }

    public TransactionReceipt transferToStudent(String toAddress, BigInteger amount) throws Exception {
        return contract.transfer(toAddress, amount).send();
    }

    public TransactionReceipt collectFromStudent(String fromAddress, BigInteger amount) throws Exception {
        return contract.collectTokenFromStudent(fromAddress, amount).send();
    }

    public TransactionReceipt mint(String toAddress, BigInteger amount) throws Exception {
        return contract.mint(toAddress, amount).send();
    }

    public TransactionReceipt transferFromStudentToAnother(String from, String to, BigInteger amount) throws Exception {
        return contract.transferOnBehalf(from, to, amount).send();
    }
}