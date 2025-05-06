package com.example.blockchain.record.keeping.controllers;

import com.alibaba.excel.EasyExcel;
import com.example.blockchain.record.keeping.dtos.CertificateDTO;
import com.example.blockchain.record.keeping.dtos.CertificateExcelRowDTO;
import com.example.blockchain.record.keeping.dtos.CertificateStudentRequest;
import com.example.blockchain.record.keeping.models.Certificate;
import com.example.blockchain.record.keeping.models.Student;
import com.example.blockchain.record.keeping.repositorys.CertificateRepository;
import com.example.blockchain.record.keeping.repositorys.StudentRepository;
import com.example.blockchain.record.keeping.services.BrevoApiEmailService;
import com.example.blockchain.record.keeping.services.CertificateExcelListener;
import com.example.blockchain.record.keeping.services.CertificateService;
import com.example.blockchain.record.keeping.services.EmailService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;
    private final EmailService emailService;
    private final BrevoApiEmailService brevoApiEmailService;
    private final StudentRepository studentRepository;
    private final CertificateRepository certificateRepository;
//---------------------------- ADMIN -------------------------------------------------------




//---------------------------- KHOA -------------------------------------------------------
//    @PreAuthorize("hasAuthority('WRITE')")
    @PostMapping("/pdt/certificate/create")
    public ResponseEntity<?> createCertificate(@RequestBody JsonNode jsonNode) {
        try {
            certificateService.createCertificate(jsonNode);
            return ResponseEntity.ok("Tạo chứng chỉ thành công");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống");
        }
    }


    @PostMapping("/pdt/certificate/upload-excel")
    public ResponseEntity<?> uploadExcel(@RequestParam("file") MultipartFile file) {
        try {
            CertificateExcelListener listener = new CertificateExcelListener(studentRepository, certificateRepository);

            EasyExcel.read(file.getInputStream(), CertificateExcelRowDTO.class, listener)
                    .sheet()
                    .doRead();
            List<CertificateExcelRowDTO> validStudents = listener.getDataList();
            List<String> errors = listener.getErrorMessages();
            if (!errors.isEmpty()) {
                return ResponseEntity.badRequest().body(errors);
            }

            // nào chạy thì mở
//            brevoApiEmailService.sendEmailsToStudentsExcel(validStudents);
            return ResponseEntity.ok("Đã đọc thành công " + listener.getDataList().size() + " dòng.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
