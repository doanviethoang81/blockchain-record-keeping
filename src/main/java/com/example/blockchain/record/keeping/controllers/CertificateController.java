package com.example.blockchain.record.keeping.controllers;

import com.alibaba.excel.EasyExcel;
import com.example.blockchain.record.keeping.dtos.CertificateDTO;
import com.example.blockchain.record.keeping.dtos.CertificateExcelRowDTO;
import com.example.blockchain.record.keeping.dtos.CertificateStudentRequest;
import com.example.blockchain.record.keeping.models.Certificate;
import com.example.blockchain.record.keeping.models.Student;
import com.example.blockchain.record.keeping.repositorys.CertificateRepository;
import com.example.blockchain.record.keeping.repositorys.StudentRepository;
import com.example.blockchain.record.keeping.response.ApiResponseBuilder;
import com.example.blockchain.record.keeping.services.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final BrevoApiEmailService brevoApiEmailService;
    private final StudentRepository studentRepository;
    private final CertificateRepository certificateRepository;
    private final UniversityService universityService;
    private final UniversityCertificateTypeService universityCertificateTypeService;
    private final UserService userService;
    private final CertificateTypeService certificateTypeService;


//---------------------------- ADMIN -------------------------------------------------------




//---------------------------- KHOA -------------------------------------------------------
    @PreAuthorize("hasAuthority('WRITE')")
    @PostMapping("/khoa/certificate/create")
    public ResponseEntity<?> createCertificate(
            @RequestParam("data") String dataJson,
            @RequestParam("img") MultipartFile image
    ) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(dataJson);
            JsonNode studentNode = jsonNode.get("student");
            String studentEmail = studentNode.get("email").asText();
            certificateService.createCertificate(jsonNode,image);
            return ApiResponseBuilder.success("Tạo chứng chỉ thành công, đã gửi email tới "+ studentEmail, null,null);
        } catch (IllegalArgumentException e) {
            return ApiResponseBuilder.badRequest(e.getMessage());
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi hệ thống");
        }
    }


    @PostMapping("/khoa/certificate/upload-excel")
    public ResponseEntity<?> uploadExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("img") MultipartFile image,
            @RequestParam("name_certificate_type") String certificateTypeName) {
        try {
            CertificateExcelListener listener = new CertificateExcelListener(
                                studentRepository,
                                certificateRepository,
                                universityService,
                                universityCertificateTypeService,
                                userService,
                                certificateTypeService,
                                certificateTypeName,
                                image
                        );
            EasyExcel.read(file.getInputStream(), CertificateExcelRowDTO.class, listener)
                    .sheet()
                    .doRead();
            List<CertificateExcelRowDTO> validStudents = listener.getDataList();
            List<String> errors = listener.getErrorMessages();
            if (!errors.isEmpty()) {
                return ApiResponseBuilder.listBadRequest("Có lỗi xảy ra", errors);
            }

            // nào chạy thì mở
            brevoApiEmailService.sendEmailsToStudentsExcel(validStudents);
            return ApiResponseBuilder.success("Đã đọc thành công " + listener.getDataList().size() + " dòng.", null, null);
        } catch (Exception e) {
            return ApiResponseBuilder.badRequest(e.getMessage());
        }
    }
}
