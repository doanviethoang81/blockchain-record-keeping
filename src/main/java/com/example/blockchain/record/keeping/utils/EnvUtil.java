package com.example.blockchain.record.keeping.utils;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvUtil {
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    public static String get(String key) {
        String value = System.getenv(key);
        if (value != null && !value.isEmpty()) {
            System.out.println("EnvUtil: " + key + "=" + value + " (from System.getenv)");
            return value;
        }
        value = System.getProperty(key);
        if (value != null && !value.isEmpty()) {
            System.out.println("EnvUtil: " + key + "=" + value + " (from System.getProperty)");
            return value;
        }
        value = dotenv.get(key);
        if (value != null && !value.isEmpty()) {
            System.out.println("EnvUtil: " + key + "=" + value + " (from .env)");
            return value;
        }
        System.err.println("Error: Environment variable " + key + " is not set");
        throw new IllegalStateException("Missing required environment variable: " + key);
    }
}
