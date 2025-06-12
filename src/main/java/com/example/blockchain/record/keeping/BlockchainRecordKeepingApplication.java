package com.example.blockchain.record.keeping;

import com.example.blockchain.record.keeping.configs.EnvLoader;
import com.example.blockchain.record.keeping.utils.FontProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.thymeleaf.context.IContext;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.AttributedCharacterIterator;
import java.time.LocalDate;
import java.util.Map;
import java.util.Random;

@SpringBootApplication
@ComponentScan(basePackages = {"com.example.blockchain.record.keeping"})
@EnableAsync
public class BlockchainRecordKeepingApplication {

    public static void main(String[] args) {
		EnvLoader.loadEnv();
		SpringApplication.run(BlockchainRecordKeepingApplication.class, args);

//		try {
//			// 1. Load ảnh nền chứng chỉ
//			BufferedImage template = ImageIO.read(new File("templateImg/degree_temp.png"));
//
//			// 2. Load và resize logo
//			BufferedImage logo = ImageIO.read(new File("templateImg/dau_moc_STU.png"));
//			BufferedImage resizedLogo = resizeImage(logo, 200, 200);
//
//			// 3. Bắt đầu vẽ
//			Graphics2D g2dPlain = template.createGraphics();
//			Graphics2D g2dBold = template.createGraphics();
//			Graphics2D g2dRed = template.createGraphics();
//			g2dPlain.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//			g2dBold.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//			g2dRed.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//
//			// 4. Vẽ logo lên vị trí (có thể thay đổi)
//			g2dPlain.drawImage(resizedLogo, 1127, 820, null);
//
//			// 5. Thiết lập font
//			Font font = new Font("SansSerif", Font.PLAIN, 26);
//			g2dPlain.setFont(font);
//			g2dPlain.setColor(Color.BLACK);
//
//			Font fontRed = new Font("SansSerif", Font.BOLD, 30);
//			g2dRed.setFont(fontRed);
//			g2dRed.setColor(Color.decode("#d72b10"));
//
//			Font fontBold = new Font("SansSerif", Font.BOLD, 26);
//			g2dBold.setFont(fontBold);
//			g2dBold.setColor(Color.black);
//
//			g2dBold.drawString("Trường Đại Học Công Nghệ Sài Gòn", 935, 250);
//			g2dRed.drawString("BẰNG KỸ SƯ", 1060, 350);
//			g2dBold.drawString("Công Nghệ Thông Tin", 1023, 400);
//			g2dBold.drawString("Đoàn Việt Hoàng", 1050, 510);
//			g2dBold.drawString("01/02/2000", 1100, 560);
//			g2dBold.drawString("2025", 1130, 610);
//			g2dBold.drawString("Giỏi", 1130, 660);
//			g2dBold.drawString("Chính quy", 1100, 710);
//
//			String address = "TP.Hồ Chí Minh";
//			String address2 = "Ho Chi Minh City,";
//
//			g2dBold.drawString("PGS.TS. Cao Hào Thi", 1200, 1047);
//			g2dRed.drawString("890121", 930, 1005);
//			g2dBold.drawString("182933", 1035, 1045);
//			g2dBold.drawString("SAIGON UNIVERSITY OF TECHNOLOGY", 165, 250);
//			g2dRed.drawString("THE DEGREE OF ENGINEERING", 160, 350);
//			g2dBold.drawString("INFORMATION TECHNOLOGY", 200, 400);
//			g2dBold.drawString("Đoàn Việt Hoàng", 280, 510);
//			g2dBold.drawString("01/02/2000", 320, 560);
//			g2dBold.drawString("2025", 360, 610);
//			g2dBold.drawString("Giỏi", 360, 660);
//			g2dBold.drawString("Chính quy", 320, 710);
//
//			g2dBold.drawString("TP.Hồ Chí Minh", 930, 800);
//			g2dBold.drawString("Ho Chi Minh City,29 september 2025", 250, 800);
//			g2dBold.drawString("18293", 170, 1035);
//
////			ENG
//
////			g2dPlain.drawString("CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM", 885, 91);
////			g2dPlain.drawString("Độc lập - Tự do - Hạnh phúc", 980, 132);
////			g2dBold.drawString("HIỆU TRƯỞNG", 1050, 211);
////			g2dPlain.drawString("cấp", 1130, 300);
////			g2dPlain.drawString("Cho:", 847, 510);
////			g2dPlain.drawString("Ngày sinh: ", 847, 560);
////			g2dPlain.drawString("Năm tốt nghiệp: ", 847, 610);
////			g2dPlain.drawString("Xếp loại tốt nghiệp: ", 847, 660);
////			g2dPlain.drawString("Hình thức đào tạo: ", 847, 710);
////			g2dPlain.drawString(",ngày ", 1133, 800);
////			g2dPlain.drawString(" tháng ", 1240, 800);
////			g2dPlain.drawString("năm ", 1355, 800);
////			g2dBold.drawString("HIỆU TRƯỞNG", 1206, 850);
//////			g2dPlain.drawString("Số hiệu:", 847, 1005);
//////			g2dPlain.drawString("Số vào sổ cấp bằng:", 847, 1045);
////			g2dPlain.drawString("THE SOCIALIST REPUBLIC OF VIETNAM", 125, 91);
////			g2dPlain.drawString("Independence - Freedom - Happiness", 165, 132);
////			g2dBold.drawString("THE RECTOR OF", 275, 211);
////			g2dPlain.drawString("confers", 340, 300);
////			g2dPlain.drawString("Upon:", 83, 510);
////			g2dPlain.drawString("Date of birth: ", 83, 560);
////			g2dPlain.drawString("Year of graduation: ", 83, 610);
////			g2dPlain.drawString("Degree classification: ", 83, 660);
////			g2dPlain.drawString("Mode of study: ", 83, 710);
//////			g2dPlain.drawString("Reg, No:", 83, 1035);
//
//
//			g2dPlain.dispose(); // Giải phóng tài nguyên
//			g2dBold.dispose(); // Giải phóng tài nguyên
//			g2dRed.dispose(); // Giải phóng tài nguyên
//
//			// 7. Lưu ảnh
//			File outputDir = new File("output");
//			if (!outputDir.exists()) outputDir.mkdirs();
//
//			File outputfile = new File(outputDir, "6.png");
//			boolean result = ImageIO.write(template, "png", outputfile);
//
//			if (result) {
//				System.out.println("✅ Đã tạo chứng chỉ tại: " + outputfile.getAbsolutePath());
//			} else {
//				System.err.println("❌ Không thể ghi file ảnh!");
//			}
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	// Hàm resize ảnh
	public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
		Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
		BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2dPlain = outputImage.createGraphics();
		g2dPlain.drawImage(resultingImage, 0, 0, null);
		g2dPlain.dispose();

		return outputImage;
	}
	private void drawCenteredText(Graphics2D g, String text, Font font, Color color, int width, int y) {
		g.setFont(font);
		g.setColor(color);
		FontMetrics metrics = g.getFontMetrics(font);
		int x = (width - metrics.stringWidth(text)) / 2;
		g.drawString(text, x, y);
	}

}
