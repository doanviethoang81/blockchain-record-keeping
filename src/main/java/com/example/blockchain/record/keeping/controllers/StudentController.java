package com.example.blockchain.record.keeping.controllers;

import com.alibaba.excel.EasyExcel;
import com.example.blockchain.record.keeping.dtos.CertificateDTO;
import com.example.blockchain.record.keeping.dtos.DegreeDTO;
import com.example.blockchain.record.keeping.dtos.request.StudentExcelRowRequest;
import com.example.blockchain.record.keeping.dtos.request.StudentRequest;
import com.example.blockchain.record.keeping.dtos.request.UpdateStudentRequest;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.StudentClassRepository;
import com.example.blockchain.record.keeping.repositorys.StudentRepository;
import com.example.blockchain.record.keeping.response.*;
import com.example.blockchain.record.keeping.services.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
        try{
            if (page < 1) page = 1;
            if (size < 1) size = 10;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            University university = universityService.getUniversityByEmail(username);

            List<Student> studentList = studentService.getAllStudentOfUniversity(
                    university.getId(), departmentName,className,studentCode,studentName);

            if ((departmentName != null && !departmentName.isEmpty())
                    || (studentCode != null && !studentCode.isEmpty())
                    || (className != null && !className.isEmpty())
                    || (studentName != null && !studentName.isEmpty())) {
                if(studentList.isEmpty()){
                    return ApiResponseBuilder.success("Không tìm thấy sinh viên!", null);
                }
            }

            List<StudentOfUniversityReponse> studentResponseList = new ArrayList<>();
            for(Student student : studentList){
                StudentOfUniversityReponse studentResponse = new StudentOfUniversityReponse(
                        student.getId(),
                        student.getName(),
                        student.getStudentClass().getName(),
                        student.getStudentClass().getDepartment().getName(),
                        student.getStudentCode(),
                        student.getEmail(),
                        student.getBirthDate(),
                        student.getCourse()
                );
                studentResponseList.add(studentResponse);
            }

            int start = (page -1) * size;
            int end = Math.min(start + size, studentResponseList.size());
            if (start >= studentResponseList.size()) {
                return ApiResponseBuilder.success("Chưa có khoa nào", null);
            }

            List<StudentOfUniversityReponse> pagedResult = studentResponseList.subList(start, end);
            PaginatedData<StudentOfUniversityReponse> data = new PaginatedData<>(pagedResult,
                    new PaginationMeta(studentResponseList.size(), pagedResult.size(), size, page,
                            (int) Math.ceil((double) studentResponseList.size() / size)));
            return ApiResponseBuilder.success(
                    "Lấy danh sách sinh viên trường thành công.",data);
        } catch (Exception e) {
            return ApiResponseBuilder.badRequest("Lỗi không lấy được dữ liệu!");
        }
    }

    // hiện các khoa của lớp (đã chọn lớp trong giao diện thêm sinh viên)
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/get-department-of-class")
    public ResponseEntity<?> getDepartmentOfClass(@RequestParam Long classId) {
        try{
            StudentClass studentClass = studentClassService.findById(classId);
            Department department = studentClass.getDepartment();
            DepartmentReponse departmentReponse = new DepartmentReponse(
                      department.getId(),
                      department.getName()
            );
            return ApiResponseBuilder.success("Thông tin khoa của lớp",departmentReponse);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi " + e.getMessage());
        }
    }

    // hiện các lớp của 1 khoa (đã chọn khoa trong giao diện thêm sinh viên)
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/get-class-of-department")
    public ResponseEntity<?> getClassOfDepartment(@RequestParam Long departmentId) {
        try{
            User user= userService.finbById(departmentId);
            Department department = departmentService.findById(user.getDepartment().getId());
            List<StudentClass> studentClassList = studentClassService.findAllClassesByDepartmentId(department.getId(),null);

            List<StudentClassReponse> studentClassReponseList = studentClassList.stream()
                    .map(s -> new StudentClassReponse(
                            s.getId(),
                            s.getName()
                    ))
                    .collect(Collectors.toList());
            if(studentClassReponseList.isEmpty()){
                return ApiResponseBuilder.success("Khoa này chưa có lớp nào",null);
            }
            return ApiResponseBuilder.success("Các lớp của khoa",studentClassReponseList);

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
                    studentRequest.getEmail(),studentRequest.getDepartmetnId());
            if(checkStudentCode !=null){
                return ApiResponseBuilder.badRequest("Mã số sinh viên đã tồn tại!");
            }
            if(checkEmialStudent !=null){
                return ApiResponseBuilder.badRequest("Email sinh viên đã tồn tại trong khoa này!");
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
            @RequestParam("file") MultipartFile file) throws IOException
    {
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
                        studentService
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
            Student student = studentService.findById(id);
            Student checkStudentCode  = studentService.findByStudentCodeOfUniversity(
                    studentRequest.getStudentCode(),student.getStudentClass().getDepartment().getUniversity().getId());
            Student checkEmialStudent = studentService.findByEmailStudentCodeOfDepartment(
                    studentRequest.getEmail(),student.getStudentClass().getDepartment().getId());
            if (checkStudentCode != null && !checkStudentCode.getId().equals(id)) {
                return ApiResponseBuilder.badRequest("Mã số sinh viên đã tồn tại!");
            }
            if (checkEmialStudent != null && !checkEmialStudent.getId().equals(id)) {
                return ApiResponseBuilder.badRequest("Email sinh viên đã tồn tại trong khoa này!");
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
            if (page < 1) page = 1;
            if (size < 1) size = 10;

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);

            List<Student> studentList= studentService.searchStudents(
                        user.getDepartment().getId(), className, studentCode,studentName);

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

            int start = (page - 1) * size;
            int end = Math.min(start + size, studentResponseList.size());

            if ((studentCode != null && !studentCode.isEmpty())
                    || (className != null && !className.isEmpty())
                    || (studentName != null && !studentName.isEmpty())) {
                if(studentList.isEmpty()){
                    return ApiResponseBuilder.success("Không tìm thấy sinh viên!", null);
                }
            }
            if (start >= studentResponseList.size()) {
                return ApiResponseBuilder.success("Khoa chưa có sinh viên", null);
            }

            List<StudentResponse> pagedResult = studentResponseList.subList(start, end);

            PaginatedData<StudentResponse> data = new PaginatedData<>(pagedResult,
                    new PaginationMeta(
                            studentResponseList.size(),
                            pagedResult.size(),
                            size,
                            page,
                            (int) Math.ceil((double) studentResponseList.size() / size)
                    ));

            return ApiResponseBuilder.success("Lấy danh sách sinh viên thành công.", data);

        } catch (Exception e) {
            return ApiResponseBuilder.badRequest("Lỗi không lấy được dữ liệu! " + e.getMessage());
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
            StudentDetailReponse studentDetailReponse= new StudentDetailReponse(
                    student.getName(),
                    student.getStudentCode(),
                    student.getEmail(),
                    student.getBirthDate(),
                    student.getCourse(),
                    student.getStudentClass().getName(),
                    student.getStudentClass().getDepartment().getName(),
                    student.getStudentClass().getDepartment().getUniversity().getName()
            );
            return ApiResponseBuilder.success("Thông chi tiết một sinh viên",studentDetailReponse);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi " + e.getMessage());
        }
    }
}
