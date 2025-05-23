package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.CertificateExcelRowDTO;
import com.example.blockchain.record.keeping.dtos.request.DegreeExcelRowRequest;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.http.*;

@Service
public class BrevoApiEmailService {

    private final TemplateEngine templateEngine;
    Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    String API_KEY = dotenv.get("BREVO_API_KEY");
    private static final int THREAD_POOL_SIZE = 10; // Số luồng tối đa
    private final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public BrevoApiEmailService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Async
    public void sendEmail(String toEmail, String name, String templateData) {
        String url = "https://api.brevo.com/v3/smtp/email";

        // 1. Render template từ Thymeleaf
        Context context = new Context();
        context.setVariable("certificateUrl", templateData);
        context.setVariable("studentName", name);
        String contentHtml = templateEngine.process("email-template", context);

        // 2. Gửi email qua Brevo API
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", API_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("sender", Map.of("name", "Thông Báo Cấp Chứng Chỉ", "email", "hoangdoanviet81@gmail.com"));
        body.put("to", List.of(Map.of("email", toEmail)));
        body.put("subject", name);
        body.put("htmlContent", contentHtml);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        System.out.println("Kết quả gửi email: " + response.getStatusCode());
    }


    public void sendEmailsToStudentsExcel(List<CertificateExcelRowDTO> students) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (CertificateExcelRowDTO student : students) {
            String email = student.getEmail();
            String name = student.getName();
            String certificateUrl = "https://yourdomain.com/certificates/" + student.getStudentCode();

            // Gọi phương thức sendEmail bất đồng bộ và thêm vào list futures
            //Trong vòng lặp, sendEmail được gọi thông qua CompletableFuture.runAsync(). Điều này có nghĩa là mỗi email sẽ được gửi trong một luồng riêng biệt mà không làm chậm quá trình gửi email cho các sinh viên khác.
            futures.add(CompletableFuture.runAsync(() -> sendEmail(email, name, certificateUrl), executorService));
        }

        // Đợi tất cả các tác vụ bất đồng bộ hoàn thành
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    //gửi thông báo cấp văn bằng cho sv
    public void sendEmailNotificationOfDiplomaIssuanceExcel(List<DegreeExcelRowRequest> students) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (DegreeExcelRowRequest student : students) {
            String email = student.getEmail();
            String name = student.getName();
            String certificateUrl = "https://yourdomain.com/certificates/" + student.getStudentCode();

            // Gọi phương thức sendEmail bất đồng bộ và thêm vào list futures
            //Trong vòng lặp, sendEmail được gọi thông qua CompletableFuture.runAsync(). Điều này có nghĩa là mỗi email sẽ được gửi trong một luồng riêng biệt mà không làm chậm quá trình gửi email cho các sinh viên khác.
            futures.add(CompletableFuture.runAsync(() -> sendEmail(email, name, certificateUrl), executorService));
        }

        // Đợi tất cả các tác vụ bất đồng bộ hoàn thành
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    @Async
    public void sendActivationEmail(String toEmail, String otpCode) {
        try {
            String url = "https://api.brevo.com/v3/smtp/email";

            // 1. Render template từ Thymeleaf
            Context context = new Context();
            context.setVariable("otpCode", otpCode);
            context.setVariable("email", toEmail);
            String contentHtml = templateEngine.process("otp-email-template", context); // tên file template

            // 2. Gửi email qua Brevo API
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", API_KEY);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("sender", Map.of("name", "Xác Minh Tài Khoản", "email", "hoangdoanviet81@gmail.com"));
            body.put("to", List.of(Map.of("email", toEmail)));
            body.put("subject", "Mã OTP xác minh tài khoản của bạn");
            body.put("htmlContent", contentHtml);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            System.out.println("Kết quả gửi OTP: " + response.getStatusCode());
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi OTP tới " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}