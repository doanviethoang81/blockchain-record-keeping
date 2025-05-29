package com.example.blockchain.record.keeping.utils;

import java.util.Random;

public class RandomString {
    public static String random12() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }
}
