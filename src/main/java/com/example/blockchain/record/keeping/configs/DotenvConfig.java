package com.example.blockchain.record.keeping.configs;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DotenvConfig {
    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing() // để local dùng file .env, còn Render thì không có file này
            .load();

    @PostConstruct
    public void init() {
        dotenv.entries().forEach(entry -> {
            if (System.getenv(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });
    }
}
