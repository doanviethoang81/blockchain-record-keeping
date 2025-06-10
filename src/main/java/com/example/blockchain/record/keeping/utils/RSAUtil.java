package com.example.blockchain.record.keeping.utils;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.security.*;
import javax.crypto.Cipher;
import java.util.Base64;
import java.util.HexFormat;

@Component
public class RSAUtil {

    // Mã hóa với private key => chuỗi HEX
    public String encryptWithPrivateKeyToHex(String plainText, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(encryptedBytes);
    }

    // Giải mã từ HEX bằng public key
    public String decryptWithPublicKeyFromHex(String hexEncrypted, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] encryptedBytes = hexToBytes(hexEncrypted);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    // Convert byte[] to hex string
    public String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    // Convert hex string to byte[]
    public byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] result = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            result[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return result;
    }
}

