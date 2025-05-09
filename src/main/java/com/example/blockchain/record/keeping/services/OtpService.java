package com.example.blockchain.record.keeping.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class OtpService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public void saveOtp(String email, String otpCode) {
        redisTemplate.opsForValue().set("otp:" + email, otpCode, 10, TimeUnit.MINUTES);
    }

    public boolean verifyOtp(String email, String inputOtp) {
        String savedOtp = redisTemplate.opsForValue().get("otp:" + email);
        return inputOtp.equals(savedOtp);
    }
}
