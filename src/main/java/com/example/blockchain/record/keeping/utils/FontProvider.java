package com.example.blockchain.record.keeping.utils;

import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.InputStream;

@Component
public class FontProvider {
    public final Font FONT_BOLD_30;
    public final Font FONT_BOLD_29;
    public final Font FONT_RED_32;
    public final Font FONT_PLAIN;
    public final Font FONT_SMALL;
    public final Font FONT_RED_BOLD;
    public final Font FONT_GREAT_VIBES;
    public final Font FONT_CRIMSONTEXT_BOLD_ITALIC;

    public FontProvider() {
        try {

            FONT_PLAIN = new Font("SansSerif", Font.PLAIN, 32);
            FONT_BOLD_29 = new Font("SansSerif", Font.BOLD, 29);
//            FONT_BOLD_26 = new Font("SansSerif", Font.BOLD, 26);
//            FONT_RED_30 = new Font("SansSerif", Font.BOLD, 30);
            FONT_SMALL = new Font("SansSerif", Font.PLAIN, 28);
            FONT_RED_BOLD = new Font("SansSerif", Font.BOLD, 38);
            InputStream is = getClass().getResourceAsStream("/fonts/GreatVibes-Regular.ttf");
            FONT_GREAT_VIBES = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(54f);

            InputStream iss = getClass().getResourceAsStream("/fonts/CrimsonText-BoldItalic.ttf");
            FONT_CRIMSONTEXT_BOLD_ITALIC = Font.createFont(Font.TRUETYPE_FONT, iss).deriveFont(26f);

            InputStream a = getClass().getResourceAsStream("/fonts/CrimsonText-BoldItalic.ttf");
            FONT_BOLD_30 = Font.createFont(Font.TRUETYPE_FONT, a).deriveFont(30f);

            InputStream b = getClass().getResourceAsStream("/fonts/CrimsonText-BoldItalic.ttf");
            FONT_RED_32 = Font.createFont(Font.TRUETYPE_FONT, b).deriveFont(32f);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load fonts", e);
        }
    }
}

