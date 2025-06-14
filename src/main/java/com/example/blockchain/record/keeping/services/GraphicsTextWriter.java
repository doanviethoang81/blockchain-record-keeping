package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.BlockchainRecordKeepingApplication;
import com.example.blockchain.record.keeping.dtos.request.CertificatePrintData;
import com.example.blockchain.record.keeping.dtos.request.DegreePrintData;
import com.example.blockchain.record.keeping.exceptions.BadRequestException;
import com.example.blockchain.record.keeping.models.Certificate;
import com.example.blockchain.record.keeping.utils.FontProvider;
import com.example.blockchain.record.keeping.utils.MicrosoftTranslator;
import com.example.blockchain.record.keeping.utils.RandomString;
import com.example.blockchain.record.keeping.utils.TextFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class GraphicsTextWriter {
    private final ImageUploadService imageUploadService;
    private final FontProvider fontProvider;

    //tạo chứng chỉ
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
            drawCenteredTextOfffset(g2d, grantor, fontProvider.FONT_PLAIN, Color.BLACK, 462, 640, 817);
            drawCenteredTextOfffset(g2d, signer, fontProvider.FONT_PLAIN, Color.BLACK, 462, 780, 817);

            g2d.dispose();

            return imageUploadService.uploadImage(template, "png");

        } catch (IOException e) {
            throw new BadRequestException("Lỗi khi tạo ảnh!");
        }
    }

    //tạo văn bằng
    public String drawDegreeText(DegreePrintData printData) {
        try {
            BufferedImage template = ImageIO.read(new File("templateImg/degree_temp.png"));
            int imageWidth = template.getWidth();

            String universityName = TextFormatter.capitalizeEachWord(printData.getUniversityName());
            String degreeTitle  = TextFormatter.toUpperCaseAll(printData.getDegreeTitle());
            String departmentName  = TextFormatter.capitalizeEachWord(printData.getDepartmentName());
            String name = TextFormatter.capitalizeEachWord(printData.getName());
            String birthDate = printData.getBirthDate();
            String graduationYear = printData.getGraduationYear();
            String rating  = TextFormatter.capitalizeFirst(printData.getRating());
            String educationMode = TextFormatter.capitalizeFirst(printData.getEducationMode());
            String day = printData.getDay();
            String month = printData.getMonth();
            String year = printData.getYear();
            String trainingLocation = printData.getTrainingLocation();
            String signer = printData.getSigner();
            String diplomaNumber = printData.getDiplomaNumber();
            String lotteryNumber = printData.getLotteryNumber();

            String universityNameEN = MicrosoftTranslator.translate(printData.getUniversityName(), "vi", "en");
            String degreeTitleEN = MicrosoftTranslator.translate(printData.getDegreeTitle(), "vi", "en").toUpperCase();
            String departmentNameEN = MicrosoftTranslator.translate(printData.getDepartmentName(), "vi", "en");
            String nameEN = TextFormatter.removeAccents(printData.getName());
            String ratingEN = MicrosoftTranslator.translate(printData.getRating(), "vi", "en");
            String educationModeEN = MicrosoftTranslator.translate(printData.getEducationMode(), "vi", "en");
//            String trainingLocationEN = MicrosoftTranslator.translate(printData.getTrainingLocation(), "vi", "en");

            Graphics2D g2d = template.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            drawCenteredTextOfffset(g2d, universityName, fontProvider.FONT_BOLD_29, Color.BLACK, imageWidth / 2 , 250,764);
            drawCenteredTextOfffset(g2d, degreeTitle, fontProvider.FONT_RED_32, Color.decode("#d72b10"), imageWidth /2, 350,764);
            drawCenteredTextOfffset(g2d, departmentName, fontProvider.FONT_BOLD_30, Color.BLACK, imageWidth / 2, 400,764);
            drawCenteredTextOfffset(g2d, name, fontProvider.FONT_BOLD_30, Color.BLACK, imageWidth / 2, 510,764);
            drawCenteredTextOfffset(g2d, birthDate, fontProvider.FONT_BOLD_30, Color.BLACK, imageWidth / 2, 560,764);
            drawCenteredTextOfffset(g2d, graduationYear, fontProvider.FONT_BOLD_30, Color.BLACK, imageWidth / 2, 610,764);
            drawCenteredTextOfffset(g2d, rating, fontProvider.FONT_BOLD_30, Color.BLACK, imageWidth / 2, 660,764);
            drawCenteredTextOfffset(g2d, educationMode, fontProvider.FONT_BOLD_30, Color.BLACK, imageWidth / 2, 710,764);

            drawText(g2d, day, fontProvider.FONT_BOLD_30, Color.BLACK, 1210, 800);
            drawText(g2d, month, fontProvider.FONT_BOLD_30, Color.BLACK, 1324, 800);
            drawText(g2d, year, fontProvider.FONT_BOLD_30, Color.BLACK, 1415, 800);

            drawRightAlignedText(g2d, trainingLocation, fontProvider.FONT_CRIMSONTEXT_BOLD_ITALIC, Color.BLACK, imageWidth / 2 -395, 800,755);

            drawText(g2d, diplomaNumber, fontProvider.FONT_RED_32, Color.decode("#d72b10"), 930, 1005);
            drawText(g2d, lotteryNumber, fontProvider.FONT_BOLD_30, Color.BLACK, 1035, 1045);

            drawRightAlignedText(g2d, signer, fontProvider.FONT_BOLD_30, Color.BLACK, imageWidth / 2, 1047,695);

//            ---------------------------------------------
            drawCenteredTextOfffset(g2d, universityNameEN, fontProvider.FONT_BOLD_29, Color.BLACK, imageWidth / 2 , 250,0);
            drawCenteredTextOfffset(g2d, degreeTitleEN, fontProvider.FONT_RED_32, Color.decode("#d72b10"), imageWidth /2, 350,0);
            drawCenteredTextOfffset(g2d, departmentNameEN, fontProvider.FONT_BOLD_30, Color.BLACK, imageWidth / 2, 400,0);
            drawCenteredTextOfffset(g2d, nameEN, fontProvider.FONT_BOLD_30, Color.BLACK, imageWidth / 2, 510,0);
            drawCenteredTextOfffset(g2d, birthDate, fontProvider.FONT_BOLD_30, Color.BLACK, imageWidth / 2, 560,0);
            drawCenteredTextOfffset(g2d, graduationYear, fontProvider.FONT_BOLD_30, Color.BLACK, imageWidth / 2, 610,0);
            drawText(g2d, ratingEN, fontProvider.FONT_BOLD_30, Color.BLACK, 343,660);
            drawCenteredTextOfffset(g2d, educationModeEN, fontProvider.FONT_BOLD_30, Color.BLACK, imageWidth / 2, 710,0);

//            drawText(g2d, day, fontProvider.FONT_BOLD_30, Color.BLACK, 1210, 800);
//            drawText(g2d, month, fontProvider.FONT_BOLD_30, Color.BLACK, 1324, 800);
//            drawText(g2d, year, fontProvider.FONT_BOLD_30, Color.BLACK, 1415, 800);
//            drawRightAlignedText(g2d, trainingLocation, fontProvider.FONT_CRIMSONTEXT_BOLD_ITALIC, Color.BLACK, imageWidth / 2 -395, 800,755);

            drawText(g2d, lotteryNumber, fontProvider.FONT_BOLD_30, Color.BLACK, 170, 1035);

            g2d.dispose();

            return imageUploadService.uploadImage(template, "png");
        } catch (IOException e) {
            throw new BadRequestException("Lỗi khi tạo ảnh!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    // xác thực in mộc
    public String certificateValidation(String imageCertificateUrl, String sealImageUrl) {
        try {
            BufferedImage certificateImage = ImageIO.read(new URL(imageCertificateUrl));

            BufferedImage sealImage = ImageIO.read(new URL(sealImageUrl));

            Image scaledSeal = sealImage.getScaledInstance(200, 200, Image.SCALE_SMOOTH);

            Graphics2D g2d = certificateImage.createGraphics();
            int x = 950;
            int y = 584;
            g2d.drawImage(scaledSeal, x, y, null);
            g2d.dispose();

            return imageUploadService.uploadImage(certificateImage, "png");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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

    //căn giua theo chieu ngang
    private void drawCenteredText(Graphics2D g, String text, Font font, Color color, int width, int y) {
        g.setFont(font);
        g.setColor(color);
        FontMetrics metrics = g.getFontMetrics(font);
        int x = (width - metrics.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

    // căn giữa tính từ xOffset
    private void drawCenteredTextOfffset(Graphics2D g, String text, Font font, Color color, int boxWidth, int y, int xOffset) {
        g.setFont(font);
        g.setColor(color);
        FontMetrics metrics = g.getFontMetrics(font);
        int x = xOffset + (boxWidth - metrics.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

    //căn trái
    private void drawLeftAlignedText(Graphics2D g, String text, Font font, Color color, int xOffset, int y) {
        g.setFont(font);
        g.setColor(color);
        g.drawString(text, xOffset, y);
    }

    //căn phải
    private void drawRightAlignedText(Graphics2D g, String text, Font font, Color color, int boxWidth, int y, int xOffset) {
        g.setFont(font);
        g.setColor(color);
        FontMetrics metrics = g.getFontMetrics(font);
        int textWidth = metrics.stringWidth(text);
        int x = xOffset + boxWidth - textWidth;
        g.drawString(text, x, y);
    }
}
