package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.components.ActivationCodeCache;
import com.example.blockchain.record.keeping.dtos.CertificateExcelRowDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final ActivationCodeCache activationCodeCache;
    private static final int THREAD_POOL_SIZE = 10; // Số luồng tối đa
    private final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);


    @Qualifier("emailExecutor")
    private final Executor emailExecutor;

    @Async
    public void sendCertificateEmail(String toEmail, String studentName, String certificateUrl) {
        try {
            Context context = new Context();
            context.setVariable("studentName", studentName);
            context.setVariable("certificateUrl", certificateUrl);

            String htmlContent = templateEngine.process("email-template", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Thông báo cấp chứng chỉ");
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            System.out.println("Gửi email lỗi tới " + toEmail + ": " + e.getMessage());
        }
    }

    public void sendEmailsToStudents(List<CertificateExcelRowDTO> students) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (CertificateExcelRowDTO student : students) {
            String email = student.getEmail();
            String name = student.getName();
            String certificateUrl = "https://yourdomain.com/certificates/" + student.getStudentCode();

            // Gọi phương thức sendEmail bất đồng bộ và thêm vào list futures
            //Trong vòng lặp, sendEmail được gọi thông qua CompletableFuture.runAsync(). Điều này có nghĩa là mỗi email sẽ được gửi trong một luồng riêng biệt mà không làm chậm quá trình gửi email cho các sinh viên khác.
//            futures.add(CompletableFuture.runAsync(() -> sendCertificateEmail(email, name, certificateUrl), executorService));
            futures.add(CompletableFuture.runAsync(() -> sendCertificateEmail(email, name, certificateUrl), emailExecutor));
        }

        // Đợi tất cả các tác vụ bất đồng bộ hoàn thành
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }



}

