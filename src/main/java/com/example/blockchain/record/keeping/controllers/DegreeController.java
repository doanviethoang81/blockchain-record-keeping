package com.example.blockchain.record.keeping.controllers;

import com.alibaba.excel.EasyExcel;
import com.example.blockchain.record.keeping.dtos.CertificateExcelRowDTO;
import com.example.blockchain.record.keeping.dtos.request.DegreeExcelRowRequest;
import com.example.blockchain.record.keeping.dtos.request.DegreeRequest;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.StudentRepository;
import com.example.blockchain.record.keeping.response.ApiResponseBuilder;
import com.example.blockchain.record.keeping.services.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DegreeController {
    private final StudentRepository studentRepository;
    private final UniversityService universityService;
    private final UserService userService;
    private final DegreeService degreeService;
    private final RatingService ratingService;
    private final EducationModelSevice educationModelSevice;
    private final DegreeTitleSevice degreeTitleSevice;
    private final StudentClassService studentClassService;
    private final StudentService studentService;
    private final DepartmentService departmentService;
    private final GraphicsTextWriter graphicsTextWriter;

    //---------------------------- ADMIN -------------------------------------------------------




    //---------------------------- KHOA -------------------------------------------------------
    // cấp văn bằng
    @PreAuthorize("hasAuthority('WRITE')")
    @PostMapping("/khoa/degree/create")
    public ResponseEntity<?> createDegree(
            @RequestBody DegreeRequest request
    ) {
        try {
            if (request == null
                    || request.getStudentId() == null
                    || request.getRatingId() == null
                    || request.getDegreeTitleId() == null
                    || request.getEducationModeId() == null
                    || !StringUtils.hasText(String.valueOf(request.getGraduationYear()))
                    || !StringUtils.hasText(String.valueOf(request.getTrainingLocation()))
                    || !StringUtils.hasText(String.valueOf(request.getSigner()))
                    || !StringUtils.hasText(String.valueOf(request.getDiplomaNumber()))
                    || !StringUtils.hasText(String.valueOf(request.getLotteryNumber()))
            ) {
                return ApiResponseBuilder.badRequest("Vui lòng nhập đầy đủ thông tin!");
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate localDate = LocalDate.parse(request.getIssueDate(), formatter);
            ZonedDateTime issueDate = localDate.atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh"));
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            ZonedDateTime oneYearAgo = now.minusYears(1);
            ZonedDateTime oneYearLater = now.plusYears(1);

            if (issueDate.isBefore(oneYearAgo) || issueDate.isAfter(oneYearLater)) {
                return ApiResponseBuilder.badRequest("Ngày cấp văn bằng phải trong vòng 1 năm trước và 1 năm sau kể từ hôm nay");
            }
            Student student = studentService.findById(request.getStudentId());
            if(student == null ){
                return ApiResponseBuilder.badRequest("Không tìm thấy sinh viên!");
            }
            if(degreeService.existsByStudent(student)){
                return ApiResponseBuilder.badRequest("Sinh viên này đã được cấp văn bằng!");
            }
            degreeService.createDegree(request);
            return ApiResponseBuilder.success("Tạo văn bằng thành công", null);
        } catch (DateTimeParseException e) {
            return ApiResponseBuilder.badRequest("Ngày cấp văn bằng không đúng định dạng dd/MM/yyyy");
        } catch (IllegalArgumentException e) {
            return ApiResponseBuilder.badRequest("Lỗi!" +e.getMessage());
        } catch (Exception e) {
            return ApiResponseBuilder.internalError(e.getMessage());
        }
    }

    // excel thiếu trùng số hiện chuwngsc chỉ
    @PreAuthorize("hasAuthority('WRITE')")
    @PostMapping("/khoa/degree/create-excel")
    public ResponseEntity<?> uploadExcel(
                @RequestParam("file") MultipartFile file) throws IOException
    {
        if(file.isEmpty()){
            return ApiResponseBuilder.badRequest("Vui lòng chọn file excel để thêm văn bằng!");
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
        String username = authentication.getName();
        User user = userService.findByUser(username);

        Long ida= user.getDepartment().getId();
        EasyExcel.read(
                file.getInputStream(),
                DegreeExcelRowRequest.class,
                new DegreeExcelListener(
                        ratingService,
                        educationModelSevice,
                        degreeTitleSevice,
                        degreeService,
                        studentService,
                        graphicsTextWriter,
                        ida
                )
        ).sheet().doRead();

        return ApiResponseBuilder.success("Tạo văn bằng thành công" , null);
    }

    // sửa
    @PreAuthorize("hasAuthority('READ')")
    @PutMapping("/khoa/update-degree/{id}")
    public ResponseEntity<?> updateCertificateType(
            @PathVariable("id")  Long id,
            @RequestBody DegreeRequest request)
    {
        try {
            Degree degree = degreeService.findById(id);
            if(degree == null){
                return ApiResponseBuilder.badRequest("Không tìm thấy văn bằng!");
            }
            if(degreeService.existsByIdAndStatus(id)){
                return ApiResponseBuilder.badRequest("Văn bằng đã được xác nhận, không chỉnh sửa được!");
            }
            if (request == null
                    || request.getRatingId() == null
                    || request.getDegreeTitleId() == null
                    || request.getEducationModeId() == null
                    || !StringUtils.hasText(String.valueOf(request.getGraduationYear()))
                    || !StringUtils.hasText(String.valueOf(request.getTrainingLocation()))
                    || !StringUtils.hasText(String.valueOf(request.getSigner()))
                    || !StringUtils.hasText(String.valueOf(request.getDiplomaNumber()))
                    || !StringUtils.hasText(String.valueOf(request.getLotteryNumber()))
            ) {
                return ApiResponseBuilder.badRequest("Vui lòng nhập đầy đủ thông tin!");
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate localDate = LocalDate.parse(request.getIssueDate(), formatter);
            ZonedDateTime issueDate = localDate.atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh"));
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            ZonedDateTime oneYearAgo = now.minusYears(1);
            ZonedDateTime oneYearLater = now.plusYears(1);
            if (issueDate.isBefore(oneYearAgo) || issueDate.isAfter(oneYearLater)) {
                return ApiResponseBuilder.badRequest("Ngày cấp văn bằng phải trong vòng 1 năm trước và 1 năm sau kể từ hôm nay");
            }
            degreeService.updateDegree(id,request);
            return ApiResponseBuilder.success("Sửa văn bằng thành công", null);
        } catch (DateTimeParseException e) {
            return ApiResponseBuilder.badRequest("Ngày cấp văn bằng không đúng định dạng dd/MM/yyyy");
        } catch (IllegalArgumentException e) {
            return ApiResponseBuilder.badRequest("Lỗi!" +e.getMessage());
        } catch (Exception e) {
            return ApiResponseBuilder.internalError(e.getMessage());
        }
    }
}
