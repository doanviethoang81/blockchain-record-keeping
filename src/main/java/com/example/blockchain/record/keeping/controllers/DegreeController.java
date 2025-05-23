package com.example.blockchain.record.keeping.controllers;

import com.alibaba.excel.EasyExcel;
import com.example.blockchain.record.keeping.dtos.CertificateExcelRowDTO;
import com.example.blockchain.record.keeping.dtos.request.DegreeExcelRowRequest;
import com.example.blockchain.record.keeping.repositorys.CertificateRepository;
import com.example.blockchain.record.keeping.repositorys.StudentRepository;
import com.example.blockchain.record.keeping.response.ApiResponseBuilder;
import com.example.blockchain.record.keeping.services.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DegreeController {
    private final BrevoApiEmailService brevoApiEmailService;
    private final StudentRepository studentRepository;
    private final CertificateRepository certificateRepository;
    private final UniversityService universityService;
    private final UniversityCertificateTypeService universityCertificateTypeService;
    private final UserService userService;
    private final CertificateTypeService certificateTypeService;
    private final DegreeService degreeService;
    private final RatingService ratingService;
    private final EducationModelSevice educationModelSevice;
    private final DegreeTitleSevice degreeTitleSevice;
    private final StudentClassService studentClassService;



    //---------------------------- ADMIN -------------------------------------------------------




    //---------------------------- KHOA -------------------------------------------------------
    // kiểm tra chứng chỉ đó đã cấp chưa cho sv đó chưa
    // kiểm tra sinh viên xem mssv trong file excel có trùng k gmail trùng k
    @PreAuthorize("hasAuthority('WRITE')")
    @PostMapping("/khoa/degree/create")
    public ResponseEntity<?> createDegree(
            @RequestParam("data") String dataJson,
            @RequestParam("idClass") Long idClass,
            @RequestParam("img") MultipartFile image
    ) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(dataJson);
            JsonNode studentNode = jsonNode.get("student");
            String studentEmail = studentNode.get("email").asText();
            degreeService.createDegree(jsonNode, idClass,image);
            return ApiResponseBuilder.success("Tạo văn bằng thành công, đã gửi email tới "+ studentEmail, null);
        } catch (IllegalArgumentException e) {
            return ApiResponseBuilder.badRequest("Lỗi!" +e.getMessage());
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi hệ thống");
        }
    }


    @PostMapping("/khoa/degree/upload-excel")
    public ResponseEntity<?> uploadExcelDegree(
            @RequestParam("file") MultipartFile file,
            @RequestParam("img") MultipartFile image
            ) {
        try {
            DegreeExcelListener listener = new DegreeExcelListener(
                    studentRepository,
                    universityService,
                    userService,
                    image,
                    ratingService,
                    educationModelSevice,
                    degreeTitleSevice,
                    degreeService,
                    studentClassService
            );
            EasyExcel.read(file.getInputStream(), DegreeExcelRowRequest.class, listener)
                    .sheet()
                    .doRead();
            List<DegreeExcelRowRequest> validStudents = listener.getDataList();
            List<String> errors = listener.getErrorMessages();
            if (!errors.isEmpty()) {
                return ApiResponseBuilder.listBadRequest("Có lỗi xảy ra", errors);
            }

            // nào chạy thì mở
//            brevoApiEmailService.sendEmailNotificationOfDiplomaIssuanceExcel(validStudents);
            return ApiResponseBuilder.success("Đã đọc thành công " + listener.getDataList().size() + " dòng.", null);
        } catch (Exception e) {
            return ApiResponseBuilder.badRequest(e.getMessage());
        }
    }
}
