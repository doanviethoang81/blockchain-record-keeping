package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.CertificateExcelRowDTO;
import com.example.blockchain.record.keeping.dtos.request.DegreeExcelRowRequest;
import com.example.blockchain.record.keeping.models.Certificate;
import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.User;
import com.example.blockchain.record.keeping.utils.QrCodeUtil;
import com.example.blockchain.record.keeping.utils.TextFormatter;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.http.*;

@Service
@RequiredArgsConstructor
public class BrevoApiEmailService {

    @Autowired
    private UserService userService;

    @Autowired
    private DepartmentService departmentService;

    private final TemplateEngine templateEngine;
    Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
    String API_KEY = dotenv.get("BREVO_API_KEY");
    private static final int THREAD_POOL_SIZE = 10; // Số luồng tối đa
    private final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    @Async
    public void sendEmail(String toEmail, String name, String universityName, String certificateUrl, String paperName) {
        String url = "https://api.brevo.com/v3/smtp/email";

        Context context = new Context();
        context.setVariable("studentName", name);
        context.setVariable("universityName", universityName);
        context.setVariable("certificateUrl", certificateUrl);
        context.setVariable("paperName", paperName);
        String contentHtml = templateEngine.process("email-template", context);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", API_KEY);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("sender", Map.of("name", "Thông Báo Cấp " + paperName, "email", "hoangdoanviet81@gmail.com"));
        body.put("to", List.of(Map.of("email", toEmail)));
        body.put("subject", name);
        body.put("htmlContent", contentHtml);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        System.out.println("Kết quả gửi email: " + response.getStatusCode());
    }


    // gửi gmail cho sinh viên
    public void sendEmailsToStudentsExcel(String email ,String name,String universityName,String certificateUrl,String paperName) {
        sendEmail(email, name,universityName,certificateUrl,paperName);
    }

    //gửi OTP
    @Async
    public void sendActivationEmail(String toEmail, String otpCode) {
        try {
            String url = "https://api.brevo.com/v3/smtp/email";

            Context context = new Context();
            context.setVariable("otpCode", otpCode);
            context.setVariable("email", toEmail);
            String contentHtml = templateEngine.process("otp-email-template", context);

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

    // gửi emial thông báo cho khoa
    @Async
    public void sendPasswordChange(Long id, String newPassword) {
        try {
            User user= userService.finbById(id);
            String toEmail = user.getEmail();
            String universityName = user.getUniversity().getName();
            String departmentName = user.getDepartment().getName();
            String url = "https://api.brevo.com/v3/smtp/email";

            Context context = new Context();
            context.setVariable("newPassword", newPassword);
            context.setVariable("email", toEmail);
            context.setVariable("universityName",universityName);
            context.setVariable("departmentName",departmentName);
            String contentHtml = templateEngine.process("password-change-notice-template", context);

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", API_KEY);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("sender", Map.of("name", "Phòng đạo tạo" , "email", "hoangdoanviet81@gmail.com"));
            body.put("to", List.of(Map.of("email", toEmail)));
            body.put("subject", "Thông báo ");
            body.put("htmlContent", contentHtml);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // gửi emial thông báo cho khoa quyền truy cập
    @Async
    public void sendPermissionNotification(Long id, String action) {
        try {
            User user= userService.finbById(id);
            String toEmail = user.getEmail();
            String universityName = user.getUniversity().getName();
            String departmentName = user.getDepartment().getName();
            String url = "https://api.brevo.com/v3/smtp/email";

            Context context = new Context();
            context.setVariable("email", toEmail);
            context.setVariable("universityName",universityName);
            context.setVariable("departmentName",departmentName);
            context.setVariable("actionType",action);
            String contentHtml = templateEngine.process("permission-notification", context);

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", API_KEY);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("sender", Map.of("name", "Phòng đạo tạo" , "email", "hoangdoanviet81@gmail.com"));
            body.put("to", List.of(Map.of("email", toEmail)));
            body.put("subject", "Thông báo ");
            body.put("htmlContent", contentHtml);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // gửi emial thông báo tài khoản khoa đã bị khóa/mở
    @Async
    public void sendPermissionToDepartment(Long id, String action) {
        try {
            User user= userService.finbById(id);
            String toEmail = user.getEmail();
            String universityName = user.getUniversity().getName();
            String departmentName = user.getDepartment().getName();
            String url = "https://api.brevo.com/v3/smtp/email";

            Context context = new Context();
            context.setVariable("email", toEmail);
            context.setVariable("universityName",universityName);
            context.setVariable("departmentName",departmentName);
            context.setVariable("actionType",action);
            String contentHtml = templateEngine.process("account-department-locked-notification", context);

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", API_KEY);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("sender", Map.of("name", "Phòng đạo tạo" , "email", "hoangdoanviet81@gmail.com"));
            body.put("to", List.of(Map.of("email", toEmail)));
            body.put("subject", "Thông báo ");
            body.put("htmlContent", contentHtml);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // gửi emial thông báo khóa tài khoản của trường (admin)
    @Async
    public void sendNoticeToUnniversity(Long id, String action) {
        try {
            User user= userService.finbById(id);
            String toEmail = user.getEmail();
            String universityName = user.getUniversity().getName();
            String url = "https://api.brevo.com/v3/smtp/email";

            Context context = new Context();
            context.setVariable("email", toEmail);
            context.setVariable("universityName",universityName);
            context.setVariable("actionType",action);
            String contentHtml = templateEngine.process("account-locked-notification", context);

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", API_KEY);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("sender", Map.of("name", "Hệ thống CertX" , "email", "hoangdoanviet81@gmail.com"));
            body.put("to", List.of(Map.of("email", toEmail)));
            body.put("subject", "Thông báo ");
            body.put("htmlContent", contentHtml);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //gửi OTP quên mật khẩu
    @Async
    public void sendOtpForgotPasswordEmail(String toEmail, String otpCode) {
        try {
            String url = "https://api.brevo.com/v3/smtp/email";

            Context context = new Context();
            context.setVariable("otpCode", otpCode);
            context.setVariable("email", toEmail);
            String contentHtml = templateEngine.process("otp-email-forgot-password-template", context);

            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", API_KEY);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("sender", Map.of("name", "Xác Minh Tài Khoản", "email", "hoangdoanviet81@gmail.com"));
            body.put("to", List.of(Map.of("email", toEmail)));
            body.put("subject", "Xác nhận OTP để khôi phục mật khẩu");
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