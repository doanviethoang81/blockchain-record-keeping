package com.example.blockchain.record.keeping.controllers;

import com.alibaba.excel.EasyExcel;
import com.example.blockchain.record.keeping.dtos.CertificateDTO;
import com.example.blockchain.record.keeping.dtos.CertificateExcelRowDTO;
import com.example.blockchain.record.keeping.dtos.CertificateStudentRequest;
import com.example.blockchain.record.keeping.dtos.request.StudentExcelRowRequest;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.CertificateRepository;
import com.example.blockchain.record.keeping.repositorys.StudentRepository;
import com.example.blockchain.record.keeping.response.*;
import com.example.blockchain.record.keeping.services.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CertificateController {

    private final CertificateService certificateService;
    private final UniversityService universityService;
    private final UserService userService;
    private final StudentService studentService;
    private final CertificateTypeService certificateTypeService;
    private final UniversityCertificateTypeService universityCertificateTypeService;
    private final GraphicsTextWriter graphicsTextWriter;

    //---------------------------- ADMIN -------------------------------------------------------
    // xem all chứng chỉ
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/admin/list-certificate")
    public ResponseEntity<?> getAllCertificate(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String universityName,
            @RequestParam(required = false) String departmentName,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String studentCode,
            @RequestParam(required = false) String studentName
    ) {
        try {
            List<Certificate> certificateList = certificateService.findByAllCertificate(
                    universityName,
                    departmentName,
                    className,
                    studentCode,
                    studentName
            );
            List<CertificateReponse> certificateReponseList = certificateList.stream()
                    .map(s -> new CertificateReponse(
                            s.getId(),
                            s.getStudent().getName(),
                            s.getIssueDate(),
                            s.getDiplomaNumber(),
                            s.getUniversityCertificateType().getCertificateType().getName(),
                            s.getCreatedAt()
                    ))
                    .collect(Collectors.toList());
            int start = (page - 1) * size;
            int end = Math.min(start + size, certificateReponseList.size());
            if (start >= certificateReponseList.size()) {
                return ApiResponseBuilder.success("Chưa có lớp nào!", null);
            }

            List<CertificateReponse> pagedResult = certificateReponseList.subList(start, end);
            PaginatedData<CertificateReponse> data = new PaginatedData<>(pagedResult,
                    new PaginationMeta(certificateReponseList.size(), pagedResult.size(), size, page ,
                            (int) Math.ceil((double) certificateReponseList.size() / size)));

            return ApiResponseBuilder.success("Danh sách chứng chỉ",data);
        } catch (IllegalArgumentException e) {
            return ApiResponseBuilder.badRequest(e.getMessage());
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi" + e.getMessage());
        }
    }

    //chi tieets 1 chung chi
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/admin/certificate-detail/{id}")
    public ResponseEntity<?> getDetailCertificate(
            @PathVariable Long id
    ) {
        try {
            Certificate certificate= certificateService.findById(id);
            CertificateDetailReponse certificateDetailReponse = new CertificateDetailReponse(
                    certificate.getId(),
                    certificate.getStudent().getName(),
                    certificate.getStudent().getStudentClass().getName(),
                    certificate.getStudent().getStudentClass().getDepartment().getName(),
                    certificate.getStudent().getStudentClass().getDepartment().getUniversity().getName(),
                    certificate.getUniversityCertificateType().getCertificateType().getName(),
                    certificate.getIssueDate(),
                    certificate.getDiplomaNumber(),
                    certificate.getStudent().getStudentCode(),
                    certificate.getStudent().getEmail(),
                    certificate.getStudent().getBirthDate(),
                    certificate.getStudent().getCourse(),
                    certificate.getImageUrl(),
                    certificate.getQrCodeUrl(),
                    certificate.getCreatedAt()
            );
            return ApiResponseBuilder.success("Danh sách chứng chỉ",certificateDetailReponse);
        } catch (IllegalArgumentException e) {
            return ApiResponseBuilder.badRequest(e.getMessage());
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi" + e.getMessage());
        }
    }




    //---------------------------- KHOA -------------------------------------------------------
    // tìm kiem sinh vien cho khoa để lấy id sinh viên -> cho thêm chứng chỉ
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/khoa/student-search")
    public ResponseEntity<?> searchStudent(
            @RequestParam(required = false) String studentCode
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username= authentication.getName();
            User user = userService.findByUser(username);
            List<Student> studentList = studentService.findByStudentOfDepartment(user.getDepartment().getId(),studentCode);

            List<StudentResponse> studentResponseList = studentList.stream()
                    .map(s -> new StudentResponse(
                            s.getId(),
                            s.getName(),
                            s.getStudentCode(),
                            s.getEmail(),
                            s.getStudentClass().getName(),
                            s.getBirthDate(),
                            s.getCourse()))
                    .collect(Collectors.toList());

            if(studentList.isEmpty()){
                return ApiResponseBuilder.success("Không tìm thấy sinh viên", null);
            }
            return ApiResponseBuilder.success("Tìm kiếm thành công", studentResponseList);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi");
        }
    }

    // kiểm tra chứng chỉ đó đã cấp chưa cho sv đó chưa
    // kiểm tra sinh viên xem mssv trong file excel có trùng k gmail trùng k
    @PreAuthorize("hasAuthority('WRITE')")
    @PostMapping("/khoa/create-certificate")
    public ResponseEntity<?> createCertificate(
            @RequestParam("data") String dataJson,
            @RequestParam(required = false) Long studentId
    ) {
        try {
            Student student = studentService.findById(studentId);
            if(student == null){
                return ApiResponseBuilder.badRequest("Không tìm thấy sinh viên!");
            }
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(dataJson);

            Long certificateTypeId = Long.valueOf(jsonNode.get("certificateTypeId").asText());
            Optional<Certificate> certificate = certificateService.existingStudentOfCertificate(studentId,certificateTypeId);
            if(!certificate.isEmpty()){
                return ApiResponseBuilder.badRequest("Sinh viên đã có loại chứng chỉ này");
            }
            certificateService.createCertificate(student, jsonNode );
            return ApiResponseBuilder.success("Tạo chứng chỉ thành công, chờ PDT duyệt ", null);
        } catch (IllegalArgumentException e) {
            return ApiResponseBuilder.badRequest(e.getMessage());
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi!");
        }
    }

    @PreAuthorize("hasAuthority('WRITE')")
    @PostMapping("/khoa/certificate/create-excel")
    public ResponseEntity<?> uploadExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("certificateTypeId") Long certificateTypeId) throws IOException {
        if(file.isEmpty()){
            return ApiResponseBuilder.badRequest("Vui lòng chọn file excel để thêm chứng chỉ!");
        }
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();

        if (!("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType)
                || "application/vnd.ms-excel".equals(contentType))
                || fileName == null
                || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            return ApiResponseBuilder.badRequest("File không đúng định dạng Excel (.xlsx hoặc .xls)");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username= authentication.getName();
        User user = userService.findByUser(username);

        CertificateType certificateType= certificateTypeService.findById(certificateTypeId);
        UniversityCertificateType universityCertificateType =
                universityCertificateTypeService.findByCartificateType(certificateType);

        EasyExcel.read(
                file.getInputStream(),
                CertificateExcelRowDTO.class,
                new CertificateExcelListener(
                        universityService,
                        studentService,
                        user.getDepartment().getId(),
                        universityCertificateType,
                        certificateService,
                        graphicsTextWriter
                )
        ).sheet().doRead();

        // nào chạy thì mở
//            brevoApiEmailService.sendEmailsToStudentsExcel(validStudents);
        return ApiResponseBuilder.success("Tạo chứng chỉ thành công" , null);
    }
}
