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

    // Mã hóa bằng private key (dành cho xác thực nguồn gốc)
//    public byte[] encryptWithPrivateKey(String plainText, PrivateKey privateKey) throws Exception {
//        Cipher cipher = Cipher.getInstance("RSA");
//        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
//        return cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
//    }

//    public String encryptWithPrivateKey(String plainText, PrivateKey privateKey) throws Exception {
//        Cipher cipher = Cipher.getInstance("RSA");
//        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
//        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
//        return HexFormat.of().formatHex(encryptedBytes); // Chuỗi hex
//    }
//
//
//    // Giải mã bằng public key
//    public String decryptWithPublicKey(byte[] encryptedBytes, PublicKey publicKey) throws Exception {
//        Cipher cipher = Cipher.getInstance("RSA");
//        cipher.init(Cipher.DECRYPT_MODE, publicKey);
//        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
//        return new String(decryptedBytes, StandardCharsets.UTF_8);
//    }
//
//    // Nếu bạn cần thêm bản Base64 để gửi dạng chuỗi
////    public String encryptToBase64WithPrivateKey(String plainText, PrivateKey privateKey) throws Exception {
////        byte[] encryptedBytes = encryptWithPrivateKey(plainText, privateKey);
////        return Base64.getEncoder().encodeToString(encryptedBytes);
////    }
//
//    public String decryptBase64WithPublicKey(String base64Encrypted, PublicKey publicKey) throws Exception {
//        byte[] encryptedBytes = Base64.getDecoder().decode(base64Encrypted);
//        return decryptWithPublicKey(encryptedBytes, publicKey);
//    }
//    //tạo chữ ký số từ dữ liệu và private key (dùng để "mã hóa" xác thực)
//    public String encryptWithPrivateKey(String plainText, PrivateKey privateKey) throws Exception {
//        Cipher cipher = Cipher.getInstance("RSA");
//        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
//        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes("UTF-8"));
//        return Base64.getEncoder().encodeToString(encryptedBytes);
//    }
//
//    public String decryptWithPublicKey(String encryptedBase64, PublicKey publicKey) throws Exception {
//        Cipher cipher = Cipher.getInstance("RSA");
//        cipher.init(Cipher.DECRYPT_MODE, publicKey);
//        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedBase64));
//        return new String(decryptedBytes, "UTF-8");
//    }

}

