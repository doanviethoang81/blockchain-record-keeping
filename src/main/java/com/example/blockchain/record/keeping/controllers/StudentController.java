package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.dtos.CertificateDTO;
import com.example.blockchain.record.keeping.dtos.DegreeDTO;
import com.example.blockchain.record.keeping.dtos.request.StudentRequest;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.response.*;
import com.example.blockchain.record.keeping.services.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class StudentController {
    private final PasswordEncoder passwordEncoder;
    private final UniversityService universityService;
    private final UserService userService;
    private final DepartmentService departmentService;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final UserPermissionService userPermissionService;
    private final StudentService studentService;
    private final CertificateService certificateService;
    private final DegreeService degreeService;
    private final StudentClassService studentClassService;

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
            User user = userService.findByUser(username);
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

    // thêm sinh viên
    @PreAuthorize("hasAuthority('WRITE')")
    @PostMapping("/pdt/create-student")
    public ResponseEntity<?> createStudent(
            @Valid @RequestBody StudentRequest studentRequest, BindingResult bindingResult
    ){
        if (bindingResult.hasErrors()) {
            // Lấy danh sách lỗi và trả về
            List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                    .collect(Collectors.toList());
            return ApiResponseBuilder.listBadRequest("Dữ liệu không hợp lệ", errors);
        }
        try{
            Student existingStudent  = studentService.findByStudentCodeOfClass(studentRequest.getStudentCode(),studentRequest.getClassId());
            Student checkEmialStudent = studentService.findByEmailStudentCodeOfDepartment(studentRequest.getEmail(),studentRequest.getDepartmetnId());
            if(existingStudent !=null){
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
}
