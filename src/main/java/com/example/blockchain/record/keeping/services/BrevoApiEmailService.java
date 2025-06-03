package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.CertificateExcelRowDTO;
import com.example.blockchain.record.keeping.dtos.request.DegreeExcelRowRequest;
import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.User;
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


    // gửi gmail cho sinh viên nhớ viết lại
//    public void sendEmailsToStudentsExcel(List<CertificateExcelRowDTO> students) {
//        List<CompletableFuture<Void>> futures = new ArrayList<>();
//        for (CertificateExcelRowDTO student : students) {
//            String email = student.getEmail();
//            String name = student.getName();
//            String certificateUrl = "https://yourdomain.com/certificates/" + student.getStudentCode();
//
//            // Gọi phương thức sendEmail bất đồng bộ và thêm vào list futures
//            //Trong vòng lặp, sendEmail được gọi thông qua CompletableFuture.runAsync(). Điều này có nghĩa là mỗi email sẽ được gửi trong một luồng riêng biệt mà không làm chậm quá trình gửi email cho các sinh viên khác.
//            futures.add(CompletableFuture.runAsync(() -> sendEmail(email, name, certificateUrl), executorService));
//        }
//
//        // Đợi tất cả các tác vụ bất đồng bộ hoàn thành
//        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
//    }

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

    //gửi OTP
    @Async
    public void sendActivationEmail(String toEmail, String otpCode) {
        try {
            String url = "https://api.brevo.com/v3/smtp/email";

            // 1. Render template từ Thymeleaf
            Context context = new Context();
            context.setVariable("otpCode", otpCode);
            context.setVariable("email", toEmail);
            String contentHtml = templateEngine.process("otp-email-template", context);

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


}