package com.example.blockchain.record.keeping.utils;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvUtil {
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    public static String get(String key) {
        // 1. Lấy từ biến môi trường thật (Render)
        String value = System.getenv(key);
        if (value != null && !value.isEmpty()) return value;

        // 2. Lấy từ system property nếu có truyền -D
        value = System.getProperty(key);
        if (value != null && !value.isEmpty()) return value;

        // 3. Lấy từ file .env (local)
        value = dotenv.get(key);
        return value;
    }
}
