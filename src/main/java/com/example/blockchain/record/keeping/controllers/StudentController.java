package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.dtos.CertificateDTO;
import com.example.blockchain.record.keeping.dtos.DegreeDTO;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.response.*;
import com.example.blockchain.record.keeping.services.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(defaultValue = "10") int size
    ){
        try{
            if (page < 1) page = 1;
            if (size < 1) size = 10;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);
            University university = universityService.getUniversityByEmail(username);

            List<Student> studentList = studentService.getAllStudentOfUniversity(university.getId());
            List<StudentResponse> studentResponseList = new ArrayList<>();
            for(Student student : studentList){
                StudentResponse studentResponse = new StudentResponse(
                        student.getId(),
                        student.getName(),
                        student.getStudentCode(),
                        student.getEmail(),
                        student.getStudentClass().getName(),
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

            List<StudentResponse> pagedResult = studentResponseList.subList(start, end);
            PaginatedData<StudentResponse> data = new PaginatedData<>(pagedResult,
                    new PaginationMeta(studentResponseList.size(), pagedResult.size(), size, page,
                            (int) Math.ceil((double) studentResponseList.size() / size)));
            return ApiResponseBuilder.success(
                    "Lấy danh sách sinh viên trường thành công.",data);
        } catch (Exception e) {
            return ApiResponseBuilder.badRequest("Lỗi không lấy được dữ liệu!");
        }
    }

    //---------------------------- KHOA -------------------------------------------------------
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/khoa/list-students")
    public ResponseEntity<?> getStudentOfClassOfDepartment(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String studentCode,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String studentName
    ) {
        try {
            if (page < 1) page = 1;
            if (size < 1) size = 10;

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);

            List<Student> studentList;

            if ((studentCode != null && !studentCode.isEmpty())
                    || (className != null && !className.isEmpty())
                    || (studentName != null && !studentName.isEmpty())) {
                studentList = studentService.searchStudents(
                        user.getDepartment().getId(), className, studentCode,studentName);
            } else {
                studentList = studentService.studentOfClassOfDepartmentList(user.getDepartment().getId());
            }

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

            if (start >= studentResponseList.size()) {
                return ApiResponseBuilder.success("Không có sinh viên nào.", null);
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
