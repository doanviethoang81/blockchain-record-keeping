package com.example.blockchain.record.keeping.utils;

import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.InputStream;

@Component
public class FontProvider {
    public final Font FONT_PLAIN;
    public final Font FONT_SMALL;
    public final Font FONT_RED_BOLD;
    public final Font FONT_GREAT_VIBES;

    public FontProvider() {
        try {
            FONT_PLAIN = new Font("SansSerif", Font.PLAIN, 32);
            FONT_SMALL = new Font("SansSerif", Font.PLAIN, 28);
            FONT_RED_BOLD = new Font("SansSerif", Font.BOLD, 38);
            InputStream is = getClass().getResourceAsStream("/fonts/GreatVibes-Regular.ttf");
            FONT_GREAT_VIBES = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(54f);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load fonts", e);
        }
    }
}
