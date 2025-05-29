package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.BlockchainRecordKeepingApplication;
import com.example.blockchain.record.keeping.dtos.request.CertificatePrintData;
import com.example.blockchain.record.keeping.exceptions.BadRequestException;
import com.example.blockchain.record.keeping.utils.FontProvider;
import com.example.blockchain.record.keeping.utils.RandomString;
import com.example.blockchain.record.keeping.utils.TextFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class GraphicsTextWriter {
    private final ImageUploadService imageUploadService;
    private final FontProvider fontProvider;

    public String drawCertificateText(CertificatePrintData printData) {
        try {
            BufferedImage template = ImageIO.read(new File("templateImg/certificate_temp.jpeg"));
            int imageWidth = template.getWidth();

            String universityName = TextFormatter.capitalizeEachWord(printData.getUniversityName());
            String name = printData.getCertificateTitle();
            String nameStudent = TextFormatter.capitalizeEachWord(printData.getStudentName());
            String departmentName = TextFormatter.capitalizeFirst(printData.getDepartmentName());
            String certificateName = TextFormatter.capitalizeFirst(printData.getCertificateName());
            String diplomaNumber = printData.getDiplomaNumber();
            String date = printData.getIssueDate();
            String grantor = printData.getGrantor();
            String signer = TextFormatter.capitalizeFirst(printData.getSigner());

            Graphics2D g2d = template.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            drawCenteredText(g2d, universityName, fontProvider.FONT_PLAIN, Color.BLACK, imageWidth, 220);
            drawCenteredText(g2d, name, fontProvider.FONT_RED_BOLD, Color.decode("#d72b10"), imageWidth, 300);
            drawCenteredText(g2d, nameStudent, fontProvider.FONT_GREAT_VIBES, Color.BLACK, imageWidth, 395);
            drawCenteredText(g2d, departmentName, fontProvider.FONT_PLAIN, Color.BLACK, imageWidth, 470);
            drawCenteredText(g2d, certificateName, fontProvider.FONT_PLAIN, Color.BLACK, imageWidth, 530);

            drawText(g2d, diplomaNumber, fontProvider.FONT_SMALL, Color.BLACK, 190, 780);
            drawText(g2d, date, fontProvider.FONT_SMALL, Color.BLACK, 857, 600);
            drawCenteredText(g2d, grantor, fontProvider.FONT_PLAIN, Color.BLACK, 462, 640, 817);
            drawCenteredText(g2d, signer, fontProvider.FONT_PLAIN, Color.BLACK, 462, 780, 817);

            g2d.dispose();

            return imageUploadService.uploadImage(template, "png");

        } catch (IOException e) {
            throw new BadRequestException("Lỗi khi tạo ảnh!");
        }
    }

    public static int getCenteredX(Graphics2D g2d, String text, int regionStart, int regionWidth) {
        FontMetrics metrics = g2d.getFontMetrics(g2d.getFont());
        int textWidth = metrics.stringWidth(text);
        return regionStart + (regionWidth - textWidth) / 2;
    }

    private void drawText(Graphics2D g, String text, Font font, Color color, int x, int y) {
        g.setFont(font);
        g.setColor(color);
        g.drawString(text, x, y);
    }

    private void drawCenteredText(Graphics2D g, String text, Font font, Color color, int width, int y) {
        g.setFont(font);
        g.setColor(color);
        FontMetrics metrics = g.getFontMetrics(font);
        int x = (width - metrics.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

    private void drawCenteredText(Graphics2D g, String text, Font font, Color color, int boxWidth, int y, int xOffset) {
        g.setFont(font);
        g.setColor(color);
        FontMetrics metrics = g.getFontMetrics(font);
        int x = xOffset + (boxWidth - metrics.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

}
