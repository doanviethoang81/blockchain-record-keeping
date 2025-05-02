package com.example.blockchain.record.keeping.components;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ActivationCodeCache {
    private final ConcurrentHashMap<String, ActivationCodeInfo> cache = new ConcurrentHashMap<>();

    public void saveCode(String email, String code, LocalDateTime expiryTime) {
        cache.put(email, new ActivationCodeInfo(code, expiryTime));
    }

    public ActivationCodeInfo getCode(String email) {
        return cache.get(email);
    }

    public boolean isCodeExpired(String email) {
        ActivationCodeInfo codeInfo = cache.get(email);
        return codeInfo == null || codeInfo.getExpiryTime().isBefore(LocalDateTime.now());
    }

    public static class ActivationCodeInfo {
        private final String code;
        private final LocalDateTime expiryTime;

        public ActivationCodeInfo(String code, LocalDateTime expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
        }

        public String getCode() {
            return code;
        }

        public LocalDateTime getExpiryTime() {
            return expiryTime;
        }
    }
}