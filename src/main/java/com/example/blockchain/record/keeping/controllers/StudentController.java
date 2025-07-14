package com.example.blockchain.record.keeping.controllers;

import com.alibaba.excel.EasyExcel;
import com.example.blockchain.record.keeping.dtos.request.StudentExcelRowRequest;
import com.example.blockchain.record.keeping.dtos.request.StudentRequest;
import com.example.blockchain.record.keeping.dtos.request.UpdateStudentRequest;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.LogRepository;
import com.example.blockchain.record.keeping.repositorys.StudentRepository;
import com.example.blockchain.record.keeping.response.*;
import com.example.blockchain.record.keeping.services.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class StudentController {
    private final UniversityService universityService;
    private final UserService userService;
    private final DepartmentService departmentService;
    private final StudentService studentService;
    private final StudentClassService studentClassService;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final HttpServletRequest httpServletRequest;
    private final LogRepository logRepository;

    //---------------------------- PDT -------------------------------------------------------
    //danh sách sinh viên của 1 trường
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/students-of-university")
    public ResponseEntity<?> getStudentofUniversity(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String departmentName,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String studentCode,
            @RequestParam(required = false) String studentName
    ){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            User user = userService.findByUser(username);

            long totalItems = studentService.countStudentsByUniversity(
                    user.getUniversity().getId(),
                    departmentName == null ? null : departmentName.trim(),
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim()
            );

            if (totalItems == 0) {
                PaginationMeta meta = new PaginationMeta(0, 0, size, page, 0);
                PaginatedData<CertificateResponse> data = new PaginatedData<>(Collections.emptyList(), meta);
                return ApiResponseBuilder.success("Không có sinh viên nào!", data);
            }

            int offset = (page - 1) * size;

            if (offset >= totalItems && totalItems > 0) {
                page = 1;
                offset = 0;
            }

            List<Student> studentList = studentService.getAllStudentOfUniversity(
                    user.getUniversity().getId(),
                    departmentName == null ? null : departmentName.trim(),
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    size,
                    offset
            );

            List<StudentOfUniversityResponse> studentOfUniversityResponseList = studentList.stream()
                    .map(s -> new StudentOfUniversityResponse(
                            s.getId(),
                            s.getName(),
                            s.getStudentClass().getName(),
                            s.getStudentClass().getDepartment().getName(),
                            s.getStudentCode(),
                            s.getEmail(),
                            s.getBirthDate(),
                            s.getCourse()
                    ))
                    .collect(Collectors.toList());

            int totalPages = (int) Math.ceil((double) totalItems / size);
            PaginationMeta meta = new PaginationMeta(totalItems, studentList.size(), size, page, totalPages);
            PaginatedData<StudentOfUniversityResponse> data = new PaginatedData<>(studentOfUniversityResponseList, meta);

            return ApiResponseBuilder.success("Danh sách sinh viên của trường", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    // hiện các khoa của lớp (đã chọn lớp trong giao diện thêm sinh viên)
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/get-department-of-class")
    public ResponseEntity<?> getDepartmentOfClass(
            @RequestParam Long classId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size)
    {
        try{
            if (page < 1) page = 1;
            if (size < 1) size = 10;
            StudentClass studentClass = studentClassService.findById(classId);
            Department department = studentClass.getDepartment();
            DepartmentResponse departmentResponse = new DepartmentResponse(
                      department.getId(),
                      department.getName()
            );
            return ApiResponseBuilder.success("Thông tin khoa của lớp", departmentResponse);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi " + e.getMessage());
        }
    }

    // hiện các lớp của 1 khoa (đã chọn khoa trong giao diện thêm sinh viên)
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/get-class-of-department")
    public ResponseEntity<?> getClassOfDepartment(@RequestParam Long departmentId,
                    @RequestParam(defaultValue = "1") int page,
                    @RequestParam(defaultValue = "10") int size
    ) {
        try{
            if (page < 1) page = 1;
            if (size < 1) size = 10;
            User user= userService.finbById(departmentId);
            Department department = departmentService.findById(user.getDepartment().getId());
            List<StudentClass> studentClassList = studentClassService.findAllClassesByDepartmentId(department.getId(),null);

            List<StudentClassResponse> studentClassResponseList = studentClassList.stream()
                    .map(s -> new StudentClassResponse(
                            s.getId(),
                            s.getName()
                    ))
                    .collect(Collectors.toList());
            if(studentClassResponseList.isEmpty()){
                return ApiResponseBuilder.success("Khoa này chưa có lớp nào!",studentClassResponseList);
            }

            int start = (page -1) * size;
            int end = Math.min(start + size, studentClassResponseList.size());
            if (start >= studentClassResponseList.size()) {
                return ApiResponseBuilder.success("Không có lớp nào!",studentClassResponseList);
            }

            List<StudentClassResponse> pagedResult = studentClassResponseList.subList(start, end);
            PaginatedData<StudentClassResponse> data = new PaginatedData<>(pagedResult,
                    new PaginationMeta(studentClassResponseList.size(), pagedResult.size(), size, page,
                            (int) Math.ceil((double) studentClassResponseList.size() / size)));
            return ApiResponseBuilder.success("Các lớp của khoa", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi " + e.getMessage());
        }
    }

    // thêm sinh viên
    @PreAuthorize("hasAuthority('WRITE')")
    @PostMapping("/pdt/create-student")
    public ResponseEntity<?> createStudent(
            @Valid @RequestBody StudentRequest studentRequest,
            BindingResult bindingResult
    ){
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                    .collect(Collectors.toList());
            return ApiResponseBuilder.listBadRequest("Dữ liệu không hợp lệ", errors);
        }
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            University university = universityService.getUniversityByEmail(username);

            Student checkStudentCode  = studentService.findByStudentCodeOfUniversity(
                    studentRequest.getStudentCode(),university.getId());
            Student checkEmialStudent = studentService.findByEmailStudentCodeOfDepartment(
                    studentRequest.getEmail(),university.getId());
            if(checkStudentCode !=null){
                return ApiResponseBuilder.badRequest("Mã số sinh viên đã tồn tại!");
            }
            if(checkEmialStudent !=null){
                return ApiResponseBuilder.badRequest("Email sinh viên đã tồn tại trong trường này!");
            }
            LocalDate birthDate = studentRequest.getBirthDate();
            if (birthDate != null) {
                LocalDate today = LocalDate.now();
                int age = Period.between(birthDate, today).getYears();
                if (age < 18) {
                    return ApiResponseBuilder.badRequest("Sinh viên phải từ 18 tuổi trở lên!");
                }
            }
            studentService.createStudent(studentRequest);
            return ApiResponseBuilder.success("Thêm sinh viên thành công", null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi" + e.getMessage());
        }
    }

    // thêm sinh viên bằng excel
    @PreAuthorize("hasAuthority('WRITE')")
    @PostMapping("/pdt/create-student-excel")
    public ResponseEntity<?> createStudentExcel(
            @RequestParam("file") MultipartFile file) throws IOException {
        if(file.isEmpty()){
            return ApiResponseBuilder.badRequest("Vui lòng chọn file excel để thêm sinh viên!");
        }
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();

        if (!("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType)
                || "application/vnd.ms-excel".equals(contentType))
                || fileName == null
                || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            return ApiResponseBuilder.badRequest("File không đúng định dạng Excel (.xlsx hoặc .xls)");
        }
        EasyExcel.read(
                file.getInputStream(),
                StudentExcelRowRequest.class,
                new StudentExcelListener(
                        studentRepository,
                        departmentService,
                        universityService,
                        studentClassService,
                        studentService,
                        passwordEncoder,
                        auditLogService,
                        httpServletRequest,
                        logRepository
                )
        ).sheet().doRead();
        return ApiResponseBuilder.success("Thêm sinh viên thành công", null);
    }

    //sửa
    @PreAuthorize("hasAuthority('READ')")
    @PutMapping("/pdt/update-student/{id}")
    public ResponseEntity<?> updateStudent(
             @Valid @PathVariable("id")  Long id,
             @RequestBody UpdateStudentRequest studentRequest,
             BindingResult bindingResult)
    {
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                    .collect(Collectors.toList());
            return ApiResponseBuilder.listBadRequest("Dữ liệu không hợp lệ", errors);
        }
        try {
            User user = userService.finbById(studentRequest.getDepartmentId());

            if(!studentClassService.existsByIdAndDepartmentId(studentRequest.getClassId(),user.getDepartment().getId())){
                return ApiResponseBuilder.badRequest("Lớp này không thuộc khoa này!");
            }
            Student student = studentService.findById(id);
            if(!student.getStudentCode().equals(studentRequest.getStudentCode())){
                Student checkStudentCode  = studentService.findByStudentCodeOfUniversity(
                        studentRequest.getStudentCode(),student.getStudentClass().getDepartment().getUniversity().getId());
                if (checkStudentCode != null && !checkStudentCode.getId().equals(id)) {
                    return ApiResponseBuilder.badRequest("Mã số sinh viên đã tồn tại!");
                }
            }
            if(!student.getEmail().equals(studentRequest.getEmail())){
                Student checkEmialStudent = studentService.findByEmailStudentCodeOfDepartment(
                        studentRequest.getEmail(),user.getUniversity().getId());

                if (checkEmialStudent != null && !checkEmialStudent.getId().equals(id)) {
                    return ApiResponseBuilder.badRequest("Email sinh viên đã tồn tại trong khoa này!");
                }
            }
            studentService.update(id, studentRequest);
            return ApiResponseBuilder.success("Sửa thông tin sinh viên thành công", null);
        } catch (Exception e) {
            return ApiResponseBuilder.badRequest(e.getMessage());
        }
    }

    //xóa
    @PreAuthorize("hasAuthority('READ')")
    @DeleteMapping("/pdt/delete-student/{id}")
    public ResponseEntity<?> deleteStudent(
            @PathVariable("id")  Long id)
    {
        try {
            if(studentService.countCertificateOfStudent(id) > 0){
                return ApiResponseBuilder.badRequest("Không thể xóa sinh viên này vì sinh viên có chứng chỉ đang tồn tại!");
            }
            if(studentService.countDegreeOfStudent(id) > 0){
                return ApiResponseBuilder.badRequest("Không thể xóa sinh viên này vì sinh viên có văn bằng đang tồn tại!");
            }
            studentService.delete(id);
            return ApiResponseBuilder.success("Xóa sinh viên thành công", null);
        } catch (Exception e) {
            return ApiResponseBuilder.badRequest(e.getMessage());
        }
    }

    //---------------------------- KHOA -------------------------------------------------------
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/khoa/list-students")
    public ResponseEntity<?> getStudentOfClassOfDepartment(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String studentCode,
            @RequestParam(required = false) String studentName
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            User user = userService.findByUser(username);

            long totalItems = studentService.countStudentOdDepartment(
                    user.getDepartment().getId(),
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim()
            );

            if (totalItems == 0) {
                PaginationMeta meta = new PaginationMeta(0, 0, size, page, 0);
                PaginatedData<CertificateResponse> data = new PaginatedData<>(Collections.emptyList(), meta);
                return ApiResponseBuilder.success("Không có sinh viên nào!", data);
            }

            int offset = (page - 1) * size;

            if (offset >= totalItems && totalItems > 0) {
                page = 1;
                offset = 0;
            }

            List<Student> studentList = studentService.searchStudents(
                    user.getDepartment().getId(),
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    size,
                    offset
            );

            List<StudentResponse> studentResponseList = studentList.stream()
                    .map(s -> new StudentResponse(
                            s.getId(),
                            s.getName(),
                            s.getStudentCode(),
                            s.getEmail(),
                            s.getStudentClass().getName(),
                            s.getBirthDate(),
                            s.getCourse()
                    ))
                    .collect(Collectors.toList());

            int totalPages = (int) Math.ceil((double) totalItems / size);
            PaginationMeta meta = new PaginationMeta(totalItems, studentList.size(), size, page, totalPages);
            PaginatedData<StudentResponse> data = new PaginatedData<>(studentResponseList, meta);

            return ApiResponseBuilder.success("Danh sách sinh viên của khoa", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    // chi tiết 1 sinh viên
    @PreAuthorize("(hasAnyRole('ADMIN', 'PDT', 'KHOA')) and hasAuthority('READ')")
    @GetMapping("/detail-student/{id}")
    public ResponseEntity<?> getDetatilStudent(
            @PathVariable("id") Long id)
    {
        try{
            Student student = studentService.findById(id);
            Department department = departmentService.findById(student.getStudentClass().getDepartment().getId());
            User user = userService.findByDepartment(department);
            StudentDetailResponse studentDetailReponse= new StudentDetailResponse(
                    student.getName(),
                    student.getStudentCode(),
                    student.getEmail(),
                    student.getBirthDate(),
                    student.getCourse(),
                    student.getStudentClass().getId(),
                    student.getStudentClass().getName(),
                    user.getId(),
                    student.getStudentClass().getDepartment().getName(),
                    student.getStudentClass().getDepartment().getUniversity().getName()
            );
            return ApiResponseBuilder.success("Thông chi tiết một sinh viên",studentDetailReponse);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi " + e.getMessage());
        }
    }
}
