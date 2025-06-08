package com.example.blockchain.record.keeping.configs;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvLoader {

    public static void loadEnv() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        setEnvIfMissing("ALCHEMY_URL", dotenv);

        // Load Database
        setEnvIfMissing("SPRING_DATASOURCE_URL", dotenv);
        setEnvIfMissing("SPRING_DATASOURCE_USERNAME", dotenv);
        setEnvIfMissing("SPRING_DATASOURCE_PASSWORD", dotenv);

        setEnvIfMissing("BREVO_API_KEY", dotenv);
        setEnvIfMissing("YOUR_BREVO_EMAIL", dotenv);
        setEnvIfMissing("YOUR_BREVO_SMTP_KEY", dotenv);

        setEnvIfMissing("REDIS_HOST", dotenv);
        setEnvIfMissing("REDIS_PASSWORD", dotenv);
    }

    private static void setEnvIfMissing(String key, Dotenv dotenv) {
        String value = System.getenv(key);
        if (value == null) {
            value = dotenv.get(key);
            if (value != null) {
                System.setProperty(key, value);
            }
        }
    }
}

