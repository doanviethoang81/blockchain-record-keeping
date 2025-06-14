package com.example.blockchain.record.keeping.utils;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.regex.Pattern;

@Component
public class TextFormatter {
    // viet hoa tât ca
    public static String capitalizeEachWord(String input) {
        if (input == null || input.isEmpty()) return input;
        String[] words = input.trim().toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty())
                sb.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    public static String capitalizeFirst(String input) {
        if (input == null || input.isEmpty()) return input;
        input = input.trim();
        return Character.toUpperCase(input.charAt(0)) + input.substring(1);
    }

    //viet hoa toàn bộ chuỗi
    public static String toUpperCaseAll(String input) {
        if (input == null) return null;
        return input.toUpperCase();
    }

    // k dấu
    public static String removeAccents(String input) {
        if (input == null) return null;
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("");
    }
}
