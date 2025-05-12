package com.example.blockchain.record.keeping.utils;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvUtil {
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    public static String get(String key) {
        String value = System.getenv(key);
        if (value != null && !value.isEmpty()) {
            return value;
        }
        value = System.getProperty(key);
        if (value != null && !value.isEmpty()) {
            return value;
        }
        value = dotenv.get(key);
        if (value != null && !value.isEmpty()) {
            return value;
        }
        throw new IllegalStateException("Missing required environment variable: " + key);
    }
}
