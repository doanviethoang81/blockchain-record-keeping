package com.example.blockchain.record.keeping.controllers;

import com.alibaba.excel.EasyExcel;
import com.example.blockchain.record.keeping.configs.Constants;
import com.example.blockchain.record.keeping.dtos.CertificateExcelDTO;
import com.example.blockchain.record.keeping.dtos.DegreeExcelDTO;
import com.example.blockchain.record.keeping.dtos.request.DegreeExcelRowRequest;
import com.example.blockchain.record.keeping.dtos.request.ListValidationRequest;
import com.example.blockchain.record.keeping.dtos.request.DegreeRequest;
import com.example.blockchain.record.keeping.enums.ActionType;
import com.example.blockchain.record.keeping.enums.Entity;
import com.example.blockchain.record.keeping.enums.LogTemplate;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.LogRepository;
import com.example.blockchain.record.keeping.response.*;
import com.example.blockchain.record.keeping.services.*;
import com.example.blockchain.record.keeping.utils.ExcelStyleUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DegreeController {
    private final UniversityService universityService;
    private final UserService userService;
    private final DegreeService degreeService;
    private final RatingService ratingService;
    private final EducationModelSevice educationModelSevice;
    private final DegreeTitleSevice degreeTitleSevice;
    private final StudentService studentService;
    private final GraphicsTextWriter graphicsTextWriter;
    private final AuditLogService auditLogService;
    private final HttpServletRequest httpServletRequest;
    private final LogRepository logRepository;
    private final NotificateService notificateService;
    private final NotificationReceiverService notificationReceiverService;
    private final NotificationWebSocketSender notificationWebSocketSender;

    //---------------------------- KHOA -------------------------------------------------------
    // tạo văn bằng
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
            Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);

            if(degreeService.existByDiplomanumber(user.getUniversity().getId(), request.getDiplomaNumber())){
                return ApiResponseBuilder.badRequest("Số hiệu văn bằng đã tồn tại!");
            }
            if(degreeService.existByLotteryNumber(user.getUniversity().getId(), request.getLotteryNumber())){
                return ApiResponseBuilder.badRequest("Số vào sổ văn bằng đã tồn tại!");
            }
            degreeService.createDegree(request,user);
            return ApiResponseBuilder.success("Tạo văn bằng thành công", null);
        } catch (DateTimeParseException e) {
            return ApiResponseBuilder.badRequest("Ngày cấp văn bằng không đúng định dạng dd/MM/yyyy");
        } catch (IllegalArgumentException e) {
            return ApiResponseBuilder.badRequest("Lỗi!" +e.getMessage());
        } catch (Exception e) {
            return ApiResponseBuilder.internalError(e.getMessage());
        }
    }

    // excel
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
                        user,
                        auditLogService,
                        httpServletRequest,
                        logRepository,
                        userService,
                        notificateService,
                        notificationReceiverService,
                        notificationWebSocketSender
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
            request.setTrainingLocation("Tp. Hồ Chí Minh");
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
            Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);

            if(degreeService.existByDiplomanumberIngnoreId(user.getUniversity().getId(), request.getDiplomaNumber(),id)){
                return ApiResponseBuilder.badRequest("Số hiệu văn bằng đã tồn tại!");
            }
            if(degreeService.existByLotteryNumberIngnoreId(user.getUniversity().getId(), request.getLotteryNumber(),id)){
                return ApiResponseBuilder.badRequest("Số vào sổ văn bằng đã tồn tại!");
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

    //danh sách văn bằng của các trường
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/admin/list-degree")
    public ResponseEntity<?> listAllDegree(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String universityName,
            @RequestParam(required = false) String departmentName,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String studentCode,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String graduationYear,
            @RequestParam(required = false) String diplomaNumber
    ) {
        try {
            List<Degree> degreeList = degreeService.listAllDegree(
                    universityName,
                    departmentName,
                    className,
                    studentCode,
                    studentName,
                    graduationYear,
                    diplomaNumber
            );
            if (departmentName != null && !departmentName.isEmpty()
                    ||className != null && !className.isEmpty()
                    ||studentCode != null && !studentCode.isEmpty()
                    ||studentName != null && !studentName.isEmpty()
                    ||graduationYear != null && !graduationYear.isEmpty()
                    ||diplomaNumber != null && !diplomaNumber.isEmpty()
            ) {
                if (degreeList.isEmpty()) {
                    return ApiResponseBuilder.success("Không tìm thấy văn bằng!",degreeList);
                }
            }
            List<DegreeResponse> degreeResponseList = degreeList.stream()
                    .map(s -> new DegreeResponse(
                            s.getId(),
                            s.getStudent().getName(),
                            s.getStudent().getStudentClass().getName(),
                            s.getStudent().getStudentClass().getDepartment().getName(),
                            s.getIssueDate(),
                            s.getStatus().getLabel(),
                            s.getGraduationYear(),
                            s.getDiplomaNumber(),
                            s.getCreatedAt()
                    ))
                    .collect(Collectors.toList());
            int start = (page - 1) * size;
            int end = Math.min(start + size, degreeResponseList.size());
            if (start >= degreeResponseList.size()) {
                return ApiResponseBuilder.success("Không có văn bằng nào!",degreeResponseList);
            }

            List<DegreeResponse> pagedResult = degreeResponseList.subList(start, end);
            PaginatedData<DegreeResponse> data = new PaginatedData<>(pagedResult,
                    new PaginationMeta(degreeResponseList.size(), pagedResult.size(), size, page ,
                            (int) Math.ceil((double) degreeResponseList.size() / size)));

            return ApiResponseBuilder.success("Danh sách văn bằng",data);
        } catch (IllegalArgumentException e) {
            return ApiResponseBuilder.badRequest(e.getMessage());
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi " + e.getMessage());
        }
    }

    //danh sách văn bằng của 1 tr
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/list-degree")
    public ResponseEntity<?> getDegreeOfUniversity(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String departmentName,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String studentCode,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String graduationYear,
            @RequestParam(required = false) String diplomaNumber
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            University university = universityService.getUniversityByEmail(username);
            Long universityId = university.getId();

            long totalItems = degreeService.countAllDegreeOfUniversity(
                    universityId,
                    departmentName == null ? null : departmentName.trim(),
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    graduationYear == null ? null : graduationYear.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim()
            );

            if (totalItems == 0) {
                PaginationMeta meta = new PaginationMeta(0, 0, size, page, 0);
                PaginatedData<CertificateResponse> data = new PaginatedData<>(Collections.emptyList(), meta);
                return ApiResponseBuilder.success("Không có văn bằng nào!", data);
            }

            int offset = (page - 1) * size;

            if (offset >= totalItems && totalItems > 0) {
                page = 1;
                offset = 0;
            }

            List<Degree> degreeList = degreeService.listAllDegreeOfUniversity(
                    universityId,
                    departmentName == null ? null : departmentName.trim(),
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    graduationYear == null ? null : graduationYear.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim(),
                    size,
                    offset
            );

            List<DegreeResponse> degreeResponseList = degreeList.stream()
                    .map(s -> new DegreeResponse(
                            s.getId(),
                            s.getStudent().getName(),
                            s.getStudent().getStudentClass().getName(),
                            s.getStudent().getStudentClass().getDepartment().getName(),
                            s.getIssueDate(),
                            s.getStatus().getLabel(),
                            s.getGraduationYear(),
                            s.getDiplomaNumber(),
                            s.getCreatedAt()
                    ))
                    .collect(Collectors.toList());

            int totalPages = (int) Math.ceil((double) totalItems / size);
            PaginationMeta meta = new PaginationMeta(totalItems, degreeList.size(), size, page, totalPages);
            PaginatedData<DegreeResponse> data = new PaginatedData<>(degreeResponseList, meta);

            return ApiResponseBuilder.success("Danh sách văn bằng của trường", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    //list văn bằng pending của 1 tr
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/list-degree-pending")
    public ResponseEntity<?> getDegreePendingOfUniversity(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String departmentName,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String studentCode,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String graduationYear,
            @RequestParam(required = false) String diplomaNumber
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            University university = universityService.getUniversityByEmail(username);
            Long universityId = university.getId();

            long totalItems = degreeService.countAllDegreeOfUniversityAndStatus(
                    universityId,
                    departmentName == null ? null : departmentName.trim(),
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    graduationYear == null ? null : graduationYear.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim(),
                    Status.PENDING.name()
            );

            if (totalItems == 0) {
                PaginationMeta meta = new PaginationMeta(0, 0, size, page, 0);
                PaginatedData<CertificateResponse> data = new PaginatedData<>(Collections.emptyList(), meta);
                return ApiResponseBuilder.success("Không có chứng chỉ nào!", data);
            }

            int offset = (page - 1) * size;

            if (offset >= totalItems && totalItems > 0) {
                page = 1;
                offset = 0;
            }

            List<Degree> degreeList = degreeService.listAllDegreeOfUniversityAndStatus(
                    universityId,
                    departmentName == null ? null : departmentName.trim(),
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    graduationYear == null ? null : graduationYear.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim(),
                    Status.PENDING.name(),
                    size,
                    offset
            );

            List<DegreeResponse> degreeResponseList = degreeList.stream()
                    .map(s -> new DegreeResponse(
                            s.getId(),
                            s.getStudent().getName(),
                            s.getStudent().getStudentClass().getName(),
                            s.getStudent().getStudentClass().getDepartment().getName(),
                            s.getIssueDate(),
                            s.getStatus().getLabel(),
                            s.getGraduationYear(),
                            s.getDiplomaNumber(),
                            s.getCreatedAt()
                    ))
                    .collect(Collectors.toList());

            int totalPages = (int) Math.ceil((double) totalItems / size);
            PaginationMeta meta = new PaginationMeta(totalItems, degreeList.size(), size, page, totalPages);
            PaginatedData<DegreeResponse> data = new PaginatedData<>(degreeResponseList, meta);

            return ApiResponseBuilder.success("Danh sách văn bằng chưa được xác nhận của trường", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    //list văn bằng approved của 1 tr
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/list-degree-approved")
    public ResponseEntity<?> getDegreeApprovedOfUniversity(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String departmentName,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String studentCode,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String graduationYear,
            @RequestParam(required = false) String diplomaNumber
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            University university = universityService.getUniversityByEmail(username);
            Long universityId = university.getId();

            long totalItems = degreeService.countAllDegreeOfUniversityAndStatus(
                    universityId,
                    departmentName == null ? null : departmentName.trim(),
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    graduationYear == null ? null : graduationYear.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim(),
                    Status.APPROVED.name()
            );

            if (totalItems == 0) {
                PaginationMeta meta = new PaginationMeta(0, 0, size, page, 0);
                PaginatedData<CertificateResponse> data = new PaginatedData<>(Collections.emptyList(), meta);
                return ApiResponseBuilder.success("Không có chứng chỉ nào!", data);
            }

            int offset = (page - 1) * size;

            if (offset >= totalItems && totalItems > 0) {
                page = 1;
                offset = 0;
            }

            List<Degree> degreeList = degreeService.listAllDegreeOfUniversityAndStatus(
                    universityId,
                    departmentName == null ? null : departmentName.trim(),
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    graduationYear == null ? null : graduationYear.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim(),
                    Status.APPROVED.name(),
                    size,
                    offset
            );

            List<DegreeResponse> degreeResponseList = degreeList.stream()
                    .map(s -> new DegreeResponse(
                            s.getId(),
                            s.getStudent().getName(),
                            s.getStudent().getStudentClass().getName(),
                            s.getStudent().getStudentClass().getDepartment().getName(),
                            s.getIssueDate(),
                            s.getStatus().getLabel(),
                            s.getGraduationYear(),
                            s.getDiplomaNumber(),
                            s.getCreatedAt()
                    ))
                    .collect(Collectors.toList());

            int totalPages = (int) Math.ceil((double) totalItems / size);
            PaginationMeta meta = new PaginationMeta(totalItems, degreeList.size(), size, page, totalPages);
            PaginatedData<DegreeResponse> data = new PaginatedData<>(degreeResponseList, meta);

            return ApiResponseBuilder.success("Danh sách văn bằng đã được xác nhận của trường", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    //list văn bằng rejected của 1 tr
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/list-degree-rejected")
    public ResponseEntity<?> getDegreeRejectedOfUniversity(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String departmentName,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String studentCode,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String graduationYear,
            @RequestParam(required = false) String diplomaNumber
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            University university = universityService.getUniversityByEmail(username);
            Long universityId = university.getId();

            long totalItems = degreeService.countAllDegreeOfUniversityAndStatus(
                    universityId,
                    departmentName == null ? null : departmentName.trim(),
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    graduationYear == null ? null : graduationYear.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim(),
                    Status.REJECTED.name()
            );

            if (totalItems == 0) {
                PaginationMeta meta = new PaginationMeta(0, 0, size, page, 0);
                PaginatedData<CertificateResponse> data = new PaginatedData<>(Collections.emptyList(), meta);
                return ApiResponseBuilder.success("Không có chứng chỉ nào!", data);
            }

            int offset = (page - 1) * size;

            if (offset >= totalItems && totalItems > 0) {
                page = 1;
                offset = 0;
            }

            List<Degree> degreeList = degreeService.listAllDegreeOfUniversityAndStatus(
                    universityId,
                    departmentName == null ? null : departmentName.trim(),
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    graduationYear == null ? null : graduationYear.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim(),
                    Status.REJECTED.name(),
                    size,
                    offset
            );

            List<DegreeResponse> degreeResponseList = degreeList.stream()
                    .map(s -> new DegreeResponse(
                            s.getId(),
                            s.getStudent().getName(),
                            s.getStudent().getStudentClass().getName(),
                            s.getStudent().getStudentClass().getDepartment().getName(),
                            s.getIssueDate(),
                            s.getStatus().getLabel(),
                            s.getGraduationYear(),
                            s.getDiplomaNumber(),
                            s.getCreatedAt()
                    ))
                    .collect(Collectors.toList());

            int totalPages = (int) Math.ceil((double) totalItems / size);
            PaginationMeta meta = new PaginationMeta(totalItems, degreeList.size(), size, page, totalPages);
            PaginatedData<DegreeResponse> data = new PaginatedData<>(degreeResponseList, meta);

            return ApiResponseBuilder.success("Danh sách văn bằng bị từ chối xác nhận của trường", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    //all văn bằng của 1 khoa
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/khoa/list-degree")
    public ResponseEntity<?> getDegreeOfDepartment(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String departmentName,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String studentCode,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String graduationYear,
            @RequestParam(required = false) String diplomaNumber
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            User user = userService.findByUser(username);

            long totalItems = degreeService.countAllDegreeOfDepartment(
                    user.getDepartment().getId(),
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    graduationYear == null ? null : graduationYear.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim()
            );

            if (totalItems == 0) {
                PaginationMeta meta = new PaginationMeta(0, 0, size, page, 0);
                PaginatedData<CertificateResponse> data = new PaginatedData<>(Collections.emptyList(), meta);
                return ApiResponseBuilder.success("Không có văn bằng nào!", data);
            }

            int offset = (page - 1) * size;

            if (offset >= totalItems && totalItems > 0) {
                page = 1;
                offset = 0;
            }

            List<Degree> degreeList = degreeService.listAllDegreeOfDepartment(
                    user.getDepartment().getId(),
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    graduationYear == null ? null : graduationYear.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim(),
                    size,
                    offset
            );

            List<DegreeResponse> degreeResponseList = degreeList.stream()
                    .map(s -> new DegreeResponse(
                            s.getId(),
                            s.getStudent().getName(),
                            s.getStudent().getStudentClass().getName(),
                            s.getStudent().getStudentClass().getDepartment().getName(),
                            s.getIssueDate(),
                            s.getStatus().getLabel(),
                            s.getGraduationYear(),
                            s.getDiplomaNumber(),
                            s.getCreatedAt()
                    ))
                    .collect(Collectors.toList());

            int totalPages = (int) Math.ceil((double) totalItems / size);
            PaginationMeta meta = new PaginationMeta(totalItems, degreeList.size(), size, page, totalPages);
            PaginatedData<DegreeResponse> data = new PaginatedData<>(degreeResponseList, meta);

            return ApiResponseBuilder.success("Danh sách văn bằng của khoa", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    //list văn bằng pending của khoa
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/khoa/list-degree-pending")
    public ResponseEntity<?> getDegreePendingOfDepartment(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String studentCode,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String graduationYear,
            @RequestParam(required = false) String diplomaNumber
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            User user = userService.findByUser(username);

            long totalItems = degreeService.countDegreeOfDepartmentAndStatus(
                    user.getDepartment().getId(),
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    graduationYear == null ? null : graduationYear.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim(),
                    Status.PENDING.name()
            );

            if (totalItems == 0) {
                PaginationMeta meta = new PaginationMeta(0, 0, size, page, 0);
                PaginatedData<CertificateResponse> data = new PaginatedData<>(Collections.emptyList(), meta);
                return ApiResponseBuilder.success("Không có văn bằng nào!", data);
            }

            int offset = (page - 1) * size;

            if (offset >= totalItems && totalItems > 0) {
                page = 1;
                offset = 0;
            }

            List<Degree> degreeList = degreeService.listAllDegreeOfDepartmentAndStatus(
                    user.getDepartment().getId(),
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    graduationYear == null ? null : graduationYear.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim(),
                    Status.PENDING.name(),
                    size,
                    offset
            );

            List<DegreeResponse> degreeResponseList = degreeList.stream()
                    .map(s -> new DegreeResponse(
                            s.getId(),
                            s.getStudent().getName(),
                            s.getStudent().getStudentClass().getName(),
                            s.getStudent().getStudentClass().getDepartment().getName(),
                            s.getIssueDate(),
                            s.getStatus().getLabel(),
                            s.getGraduationYear(),
                            s.getDiplomaNumber(),
                            s.getCreatedAt()
                    ))
                    .collect(Collectors.toList());

            int totalPages = (int) Math.ceil((double) totalItems / size);
            PaginationMeta meta = new PaginationMeta(totalItems, degreeList.size(), size, page, totalPages);
            PaginatedData<DegreeResponse> data = new PaginatedData<>(degreeResponseList, meta);

            return ApiResponseBuilder.success("Danh sách văn bằng chưa được xác thực của khoa", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    //list văn bằng approved của khoa
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/khoa/list-degree-approved")
    public ResponseEntity<?> getDegreeApprovedOfDepartment(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String studentCode,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String graduationYear,
            @RequestParam(required = false) String diplomaNumber
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            User user = userService.findByUser(username);

            long totalItems = degreeService.countDegreeOfDepartmentAndStatus(
                    user.getDepartment().getId(),
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    graduationYear == null ? null : graduationYear.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim(),
                    Status.APPROVED.name()
            );

            if (totalItems == 0) {
                PaginationMeta meta = new PaginationMeta(0, 0, size, page, 0);
                PaginatedData<CertificateResponse> data = new PaginatedData<>(Collections.emptyList(), meta);
                return ApiResponseBuilder.success("Không có văn bằng nào!", data);
            }

            int offset = (page - 1) * size;

            if (offset >= totalItems && totalItems > 0) {
                page = 1;
                offset = 0;
            }

            List<Degree> degreeList = degreeService.listAllDegreeOfDepartmentAndStatus(
                    user.getDepartment().getId(),
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    graduationYear == null ? null : graduationYear.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim(),
                    Status.APPROVED.name(),
                    size,
                    offset
            );

            List<DegreeResponse> degreeResponseList = degreeList.stream()
                    .map(s -> new DegreeResponse(
                            s.getId(),
                            s.getStudent().getName(),
                            s.getStudent().getStudentClass().getName(),
                            s.getStudent().getStudentClass().getDepartment().getName(),
                            s.getIssueDate(),
                            s.getStatus().getLabel(),
                            s.getGraduationYear(),
                            s.getDiplomaNumber(),
                            s.getCreatedAt()
                    ))
                    .collect(Collectors.toList());

            int totalPages = (int) Math.ceil((double) totalItems / size);
            PaginationMeta meta = new PaginationMeta(totalItems, degreeList.size(), size, page, totalPages);
            PaginatedData<DegreeResponse> data = new PaginatedData<>(degreeResponseList, meta);

            return ApiResponseBuilder.success("Danh sách văn bằng đã được xác thực của khoa", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    //list văn bằng rejected của khoa
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/khoa/list-degree-rejected")
    public ResponseEntity<?> getDegreeRejectedOfDepartment(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String studentCode,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String graduationYear,
            @RequestParam(required = false) String diplomaNumber
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            User user = userService.findByUser(username);

            long totalItems = degreeService.countDegreeOfDepartmentAndStatus(
                    user.getDepartment().getId(),
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    graduationYear == null ? null : graduationYear.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim(),
                    Status.REJECTED.name()
            );

            if (totalItems == 0) {
                PaginationMeta meta = new PaginationMeta(0, 0, size, page, 0);
                PaginatedData<CertificateResponse> data = new PaginatedData<>(Collections.emptyList(), meta);
                return ApiResponseBuilder.success("Không có văn bằng nào!", data);
            }

            int offset = (page - 1) * size;

            if (offset >= totalItems && totalItems > 0) {
                page = 1;
                offset = 0;
            }

            List<Degree> degreeList = degreeService.listAllDegreeOfDepartmentAndStatus(
                    user.getDepartment().getId(),
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    graduationYear == null ? null : graduationYear.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim(),
                    Status.REJECTED.name(),
                    size,
                    offset
            );

            List<DegreeResponse> degreeResponseList = degreeList.stream()
                    .map(s -> new DegreeResponse(
                            s.getId(),
                            s.getStudent().getName(),
                            s.getStudent().getStudentClass().getName(),
                            s.getStudent().getStudentClass().getDepartment().getName(),
                            s.getIssueDate(),
                            s.getStatus().getLabel(),
                            s.getGraduationYear(),
                            s.getDiplomaNumber(),
                            s.getCreatedAt()
                    ))
                    .collect(Collectors.toList());

            int totalPages = (int) Math.ceil((double) totalItems / size);
            PaginationMeta meta = new PaginationMeta(totalItems, degreeList.size(), size, page, totalPages);
            PaginatedData<DegreeResponse> data = new PaginatedData<>(degreeResponseList, meta);

            return ApiResponseBuilder.success("Danh sách văn bằng bị từ chối xác thực của khoa", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    //xác nhận 1 văn bằng
    @PreAuthorize("hasAuthority('READ')")
    @PostMapping("/pdt/degree-validation/{id}")
    public ResponseEntity<?> degreeValidation(@PathVariable("id") Long id){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);
            Degree degree = degreeService.findByIdAndStatus(id, Status.APPROVED);
            if(degree != null){
                return ApiResponseBuilder.badRequest("Văn bằng này đã được xác nhận rồi!");
            }
            degreeService.degreeValidation(user,id);
            return ApiResponseBuilder.success("Xác nhận thành công ", null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi " + e.getMessage());
        }
    }

    //từ chối xac nhận 1 văn bằng
    @PreAuthorize("hasAuthority('READ')")
    @PostMapping("/pdt/degree-rejected/{id}")
    public ResponseEntity<?> degreeRejected(@PathVariable("id") Long id){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);

            Degree degree = degreeService.findByIdAndStatus(id, Status.APPROVED);
            if(degree != null){
                return ApiResponseBuilder.badRequest("Văn bằng này đã được xác nhận rồi!");
            }
            degreeService.degreeRejected(id,user);
            return ApiResponseBuilder.success("Từ chối Xác nhận thành công ", null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi " + e.getMessage());
        }
    }

    //xác nhận 1 list văn bằng
    @PreAuthorize("hasAuthority('READ')")
    @PostMapping("/pdt/confirm-degree-list")
    public ResponseEntity<?> confirmDegreeList(@RequestBody ListValidationRequest request) {
        try {
            if (request == null || request.getIds() == null || request.getIds().isEmpty()) {
                return ApiResponseBuilder.badRequest("Vui lòng chọn văn bằng cần xác nhận!");
            }

            List<Long> ids = request.getIds();
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);

            List<String> alreadyValidated = new ArrayList<>();

            for (Long id : ids) {
                Degree degree = degreeService.findByIdAndStatus(id, Status.APPROVED);
                if (degree != null) {
                    alreadyValidated.add("Văn bằng ID " + id + " đã được xác nhận!");
                }
            }

            if (!alreadyValidated.isEmpty()) {
                return ApiResponseBuilder.listBadRequest(
                        "Không thể xác nhận vì có văn bằng đã được xác nhận.",
                        alreadyValidated
                );
            }

            degreeService.approveDegreeList(ids, user, httpServletRequest);
            return ApiResponseBuilder.success("Xác nhận tất cả văn bằng thành công", null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    //từ chối xác nhận 1 list văn bằng
    @PreAuthorize("hasAuthority('READ')")
    @PostMapping("/pdt/reject-a-list-of-degree")
    public ResponseEntity<?> rejectAListOfDegree (@RequestBody ListValidationRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);

            if (request == null || request.getIds() == null || request.getIds().isEmpty()) {
                return ApiResponseBuilder.badRequest("Vui lòng chọn văn bằng cần từ chối xác nhận!");
            }
            List<Long> ids = request.getIds();
            List<String> alreadyValidated = new ArrayList<>();

            for (Long id : ids) {
                Degree degree = degreeService.findByIdAndStatus(id, Status.APPROVED);
                if (degree != null) {
                    alreadyValidated.add("Văn bằng ID " + id + " đã được xác nhận!");
                }
            }

            if (!alreadyValidated.isEmpty()) {
                return ApiResponseBuilder.listBadRequest("Không thể xác nhận vì có văn bằng đã được xác nhận.",alreadyValidated
                );
            }
            degreeService.rejectDegreeList(ids, user);
            return ApiResponseBuilder.success("Từ chối xác nhận danh sách văn bằng thành công", null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    // chi tiết 1 văn bằng
    @PreAuthorize("(hasAnyRole('ADMIN', 'PDT', 'KHOA','STUDENT')) and hasAuthority('READ')")
    @GetMapping("/degree-detail/{id}")
    public ResponseEntity<?> degreeDetail(@PathVariable Long id){
        Degree degree = degreeService.findById(id);
        if(degree == null){
            return ApiResponseBuilder.badRequest("Không tìm thấy văn bằng có id ="+ id);
        }
        DegreeDetailResponse degreeDetailResponse = new DegreeDetailResponse();

        String ipfsUrl = degree.getIpfsUrl() != null ? Constants.IPFS_URL + degree.getIpfsUrl() : null;

        degreeDetailResponse.setId(degree.getId());
        degreeDetailResponse.setStudentId(degree.getStudent().getId());
        degreeDetailResponse.setNameStudent(degree.getStudent().getName());
        degreeDetailResponse.setClassName(degree.getStudent().getStudentClass().getName());
        degreeDetailResponse.setDepartmentName(degree.getStudent().getStudentClass().getDepartment().getName());
        degreeDetailResponse.setUniversity(degree.getStudent().getStudentClass().getDepartment().getUniversity().getName());
        degreeDetailResponse.setStudentCode(degree.getStudent().getStudentCode());
        degreeDetailResponse.setIssueDate(degree.getIssueDate());
        degreeDetailResponse.setGraduationYear(degree.getGraduationYear());
        degreeDetailResponse.setEmail(degree.getStudent().getEmail());
        degreeDetailResponse.setBirthDate(degree.getStudent().getBirthDate());
        degreeDetailResponse.setRatingId(degree.getRating().getId());
        degreeDetailResponse.setRatingName(degree.getRating().getName());
        degreeDetailResponse.setDegreeTitleId(degree.getDegreeTitle().getId());
        degreeDetailResponse.setDegreeTitleName(degree.getDegreeTitle().getName());
        degreeDetailResponse.setEducationModeId(degree.getEducationMode().getId());
        degreeDetailResponse.setEducationModeName(degree.getEducationMode().getName());
        degreeDetailResponse.setCourse(degree.getStudent().getCourse());
        degreeDetailResponse.setSigner(degree.getSigner());
        degreeDetailResponse.setStatus(degree.getStatus());
        degreeDetailResponse.setImageUrl(degree.getImageUrl());
        degreeDetailResponse.setIpfsUrl(ipfsUrl);
        degreeDetailResponse.setQrCodeUrl(degree.getQrCode());
        degreeDetailResponse.setTransactionHash(degree.getBlockchainTxHash());
        degreeDetailResponse.setDiplomaNumber(degree.getDiplomaNumber());
        degreeDetailResponse.setLotteryNumber(degree.getLotteryNumber());
        degreeDetailResponse.setCreatedAt(degree.getUpdatedAt());
        return ApiResponseBuilder.success("Chi tiết văn bằng", degreeDetailResponse);
    }

    //văn bằng cua 1 sinh vien
    @GetMapping("/student/degree")
    public ResponseEntity<?> degreeOfStudent(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        try {
            if (page < 1) page = 1;
            if (size < 1) size = 10;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Student student = studentService.findByEmail(username);
            List<DegreeResponse> degrees = degreeService.degreeOfStudent(student.getId());

            if (degrees.isEmpty()) {
                return ApiResponseBuilder.success("Sinh viên chưa có văn bằng!", degrees);
            }
            int start = (page - 1) * size;
            int end = Math.min(start + size, degrees.size());
            if (start >= degrees.size()) {
                return ApiResponseBuilder.success("Không có văn bằng nào!", degrees);
            }


            List<DegreeResponse> pagedResult = degrees.subList(start, end);
            PaginatedData<DegreeResponse> data = new PaginatedData<>(pagedResult,
                    new PaginationMeta(degrees.size(), pagedResult.size(), size, page ,
                            (int) Math.ceil((double) degrees.size() / size)));


            return ApiResponseBuilder.success("Văn bằng của sinh viên", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    //xuat excel
    @GetMapping("/pdt/export-degree")
    public void exportDegree(
            @RequestParam(name = "type", required = false) String type,
            HttpServletResponse response) throws IOException
    {
        String status = null;
        if (type != null) {
            try {
                status = type;
            } catch (IllegalArgumentException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Type không đúng định dạng!");
                return;
            }
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");

        String fileName = URLEncoder.encode("van_bang_" + (type == null ? "all" : type.toLowerCase()) , StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");

        List<DegreeExcelDTO> data = degreeService.getAllDegreeDTOs(status);

        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        if (type == null) {
            status = "Danh sách tất cả văn bằng";
        } else {
            switch (type) {
                case "approved":
                    status = "Danh sách văn bằng đã xác nhận";
                    break;
                case "rejected":
                    status = "Danh sách văn bằng đã từ chối";
                    break;
                case "pending":
                    status = "Danh sách văn bằng chờ duyệt";
                    break;
                default:
                    status = "Không rõ trạng thái";
            }
        }
        // ghi log
        String ipAdress = auditLogService.getClientIp(httpServletRequest);
        Log log = new Log();
        log.setUser(auditLogService.getCurrentUser());
        log.setActionType(ActionType.EXPORT_EXCEL);
        log.setEntityName(Entity.certificates);
        log.setEntityId(null);
        log.setDescription(LogTemplate.EXPORT_EXCEL.format(status));
        log.setCreatedAt(vietnamTime.toLocalDateTime());
        log.setIpAddress(ipAdress);
        logRepository.save(log);

        EasyExcel.write(response.getOutputStream(), DegreeExcelDTO.class)
                .registerWriteHandler(ExcelStyleUtil.certificateStyleStrategy())
                .sheet("Danh sách văn bằng " + type)
                .doWrite(data);
    }

    //xóa 1 ch ch
    @PreAuthorize("hasAuthority('READ')")
    @DeleteMapping("/pdt/delete-degree/{id}")
    public ResponseEntity<?> deleteDegree(
            @PathVariable("id")  Long id)
    {
        try {
            Degree degree= degreeService.findByIdAndStatus(id,Status.PENDING);
            if(degree == null){
                return ApiResponseBuilder.badRequest("Không xóa được văn bằng này!");
            }
            degreeService.delete(id);
            return ApiResponseBuilder.success("Xóa văn bàng thành công", null);
        } catch (Exception e) {
            return ApiResponseBuilder.badRequest(e.getMessage());
        }
    }

    //xóa 1 list ch ch
    @PreAuthorize("hasAuthority('READ')")
    @DeleteMapping("/pdt/delete-degree-list")
    public ResponseEntity<?> deleteDegreeList(@RequestBody ListValidationRequest request) {
        try {
            if (request == null || request.getIds() == null || request.getIds().isEmpty()) {
                return ApiResponseBuilder.badRequest("Vui lòng chọn văn bằng cần xóa!");
            }

            List<Long> ids = request.getIds();

            List<String> alreadyValidated = new ArrayList<>();

            for (Long id : ids) {
                Degree degree= degreeService.findByIdAndStatus(id,Status.PENDING);
                if(degree == null){
                    alreadyValidated.add("Không xóa được văn bằng!");
                }
            }

            if (!alreadyValidated.isEmpty()) {
                return ApiResponseBuilder.listBadRequest(
                        "Không thể xóa văn bằng!",
                        alreadyValidated
                );
            }

            for (Long id :ids){
                degreeService.delete(id);
            }
            return ApiResponseBuilder.success("Xóa list văn bằng thành công", null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }
}
