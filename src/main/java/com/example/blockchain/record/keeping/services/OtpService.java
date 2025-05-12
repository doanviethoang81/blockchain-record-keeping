package com.example.blockchain.record.keeping.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class OtpService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final long EXPIRE_TIME = 10 * 60;//10 phut

    public void saveOtp(String email, String otpCode) {
        redisTemplate.opsForValue().set("otp:" + email, otpCode, Duration.ofSeconds(EXPIRE_TIME));
    }

    public boolean verifyOtp(String email, String otp) {
        String key = "otp:" + email;
        String savedOtp = redisTemplate.opsForValue().get(key);

        if (savedOtp != null && savedOtp.equals(otp)) {
            redisTemplate.delete(key); //xóa
            return true;
        }
        return false;
    }
}
