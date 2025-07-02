package com.example.blockchain.record.keeping.configs;

import com.example.blockchain.record.keeping.utils.EnvUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigInteger;

@Configuration
public class BlockchainConfig {

    String ALCHEMY_URL = EnvUtil.get("ALCHEMY_URL");
    String METAMASK_PRIVATE_KEY = EnvUtil.get("METAMASK_PRIVATE_KEY");

    @Bean
    public Web3j web3j() {
        return  Web3j.build(new HttpService(ALCHEMY_URL));
    }

    @Bean
    public Credentials credentials() {
        return Credentials.create(METAMASK_PRIVATE_KEY);
    }

    @Bean
    public ContractGasProvider contractGasProvider() {
        BigInteger gasPrice = BigInteger.valueOf(30_000_000_000L); // 30 Gwei
        BigInteger gasLimit = BigInteger.valueOf(600_000);
        return new StaticGasProvider(gasPrice, gasLimit);
    }
}
