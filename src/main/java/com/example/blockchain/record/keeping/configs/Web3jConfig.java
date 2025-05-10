package com.example.blockchain.record.keeping.configs;

import com.example.blockchain.record.keeping.utils.EnvUtil;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Configuration
public class Web3jConfig {
    @Bean
    public Web3j web3j() {
//        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
//        String alchemyUrl = dotenv.get("ALCHEMY_URL");
        String alchemyUrl = EnvUtil.get("ALCHEMY_URL");

        return Web3j.build(new HttpService(alchemyUrl));
    }
}
