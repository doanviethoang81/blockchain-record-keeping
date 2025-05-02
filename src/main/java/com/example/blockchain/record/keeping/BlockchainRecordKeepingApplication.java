package com.example.blockchain.record.keeping;

import com.example.blockchain.record.keeping.configs.EnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BlockchainRecordKeepingApplication {

	public static void main(String[] args) {
		EnvLoader.loadEnv();
		SpringApplication.run(BlockchainRecordKeepingApplication.class, args);
	}

}
