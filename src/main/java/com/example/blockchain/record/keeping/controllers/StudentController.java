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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
//    @PreAuthorize("hasAuthority('READ')")
//    @GetMapping("/pdt/list-students")
//    public ResponseEntity<?> getStudentofUniversity(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size
//    ){
//        try{
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            String username = authentication.getName();
//            User user = userService.findByUser(username);
//            University university = universityService.getUniversityByEmail(username);
//
////
////            List<Department> department = departmentService.listDepartmentOfUniversity(university);
////
////            List<StudentClass> studentClassList =
//
//            List<Student> studentList = studentService.getStudentsWithCertificatesByUniversity(university.getId());
//
//            List<StudentWithCertificateResponse> studentWithCertificateResponseList = new ArrayList<>();
//
//            for (Student student : studentList) {
//                //1 list chứng chỉ
//                List<Degree> degreeList = degreeService.listDegreeOfStudent(student);
//
//                List<Certificate> certificateList = certificateService.listCertificateOfStudent(student);
//
//                List<DegreeDTO> degreeDTOList = degreeList.stream()
//                        .map(u -> new DegreeDTO(
//                                u.getIssueDate(),
//                                u.getGraduationYear(),
//                                u.getEducationMode().getName(),
//                                u.getTrainingLocation(),
//                                u.getSigner(),
//                                u.getDiplomaNumber(),
//                                u.getLotteryNumber(),
//                                u.getBlockchainTxHash(),
//                                u.getRating().getName(),
//                                u.getDegreeTitle().getName(),
//                                u.getImageUrl(),
//                                u.getCreatedAt()
//                        ))
//                        .collect(Collectors.toList());
//
//                List<CertificateDTO> certificateDTOList = certificateList.stream()
//                        .map(u -> new CertificateDTO(
//                                u.getUniversityCertificateType().getCertificateType().getName(),
//                                u.getIssueDate(),
//                                u.getDiplomaNumber(),
//                                u.getBlockchainTxHash(),
//                                u.getImageUrl(),
//                                u.getQrCodeUrl(),
//                                u.getCreatedAt()
//                        ))
//                        .collect(Collectors.toList());
//
//
//
//                StudentWithCertificateResponse studentWithCertificateResponse = new StudentWithCertificateResponse(
//                        user.getUniversity().getName(),
//                        user.getDepartment().getName(),
//                        student.getStudentClass().getName(),
//                        student.getName(),
//                        student.getStudentCode(),
//                        student.getEmail(),
//                        student.getBirthDate(),
//                        student.getCourse(),
//                        degreeDTOList,
//                        certificateDTOList
//                );
//                studentWithCertificateResponseList.add(studentWithCertificateResponse);
//            }
//
//            int start = page * size;
//            int end = Math.min(start + size, studentWithCertificateResponseList.size());
//            if (start >= studentWithCertificateResponseList.size()) {
//                return ApiResponseBuilder.success("Chưa có khoa nào", null);
//            }
//
//            List<StudentWithCertificateResponse> pagedResult = studentWithCertificateResponseList.subList(start, end);
//            PaginatedData<StudentWithCertificateResponse> data = new PaginatedData<>(pagedResult,
//                    new PaginationMeta(studentWithCertificateResponseList.size(), pagedResult.size(), size, page + 1,
//                            (int) Math.ceil((double) studentWithCertificateResponseList.size() / size)));
//
//            return ApiResponseBuilder.success(
//                    "Lấy danh sách user khoa thành công.",data);
//        } catch (Exception e) {
//            return ApiResponseBuilder.badRequest("Lỗi không lấy được dữ liệu!");
//        }
//    }

    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/students-of-university")
    public ResponseEntity<?> getStudentsOfUniversity(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            User user = userService.findByUser(username);

            University university =user.getUniversity();
            List<DepartmentWithClassWithStudentResponse> students = studentService.getStudentsWithCertificatesByUniversity(university);
            int start = page * size;
            int end = Math.min(start + size, students.size());
            if (start >= students.size()) {
                return ApiResponseBuilder.success("Chưa có khoa nào", null);
            }
            List<DepartmentWithClassWithStudentResponse> pagedResult = students.subList(start, end);
            PaginatedData<DepartmentWithClassWithStudentResponse> data = new PaginatedData<>(pagedResult,
                    new PaginationMeta(students.size(), pagedResult.size(), size, page + 1,
                            (int) Math.ceil((double) students.size() / size)));

            return ApiResponseBuilder.success(
                    "Lấy danh sách sinh viên của trường thành công.",data);
        } catch (Exception e) {
            return ApiResponseBuilder.badRequest("Lỗi không lấy được dữ liệu!");
        }
    }

    //---------------------------- KHOA -------------------------------------------------------
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/khoa/list-students")
    public ResponseEntity<?> getStudentOfClassOfDepartment(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);

            List<ClassWithStudentsResponse> classWithStudentsResponseList = studentService.studentOfClassOfDepartmentList(user.getDepartment().getId());

            int start = page * size;
            int end = Math.min(start + size, classWithStudentsResponseList.size());
            if (start >= classWithStudentsResponseList.size()) {
                return ApiResponseBuilder.success("Chưa có khoa nào", null);
            }

            List<ClassWithStudentsResponse> pagedResult = classWithStudentsResponseList.subList(start, end);
            PaginatedData<ClassWithStudentsResponse> data = new PaginatedData<>(pagedResult,
                    new PaginationMeta(classWithStudentsResponseList.size(), pagedResult.size(), size, page + 1,
                            (int) Math.ceil((double) classWithStudentsResponseList.size() / size)));

            return ApiResponseBuilder.success(
                    "Lấy danh sách sinh viên của khoa theo từng lớp thành công.",
                    data);
        } catch (Exception e) {
            return ApiResponseBuilder.badRequest("Lỗi không lấy được dữ liệu!");
        }
    }

    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/khoa/search-student-code")
    public ResponseEntity<?> searchCertificates(@RequestParam String mssv) {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);


            Optional<Student> result = studentService.findByStudentCodeAndDepartmentId(mssv,user.getDepartment().getId());

            if (result.isPresent()) {
                Student student = result.get();
                StudentResponse studentResponse = new StudentResponse(
                        student.getName(),
                        student.getStudentCode(),
                        student.getEmail(),
                        student.getStudentClass().getName(),
                        student.getBirthDate(),
                        student.getCourse()
                );
                return ApiResponseBuilder.success("Tìm thành công", studentResponse);
            }
            else{
                return ApiResponseBuilder.success("Không tìm thấy sinh viên", null);
            }
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi");
        }
    }

//    @GetMapping("/students")
//    public ResponseEntity<?> getStudentsByDepartment(
//            @RequestParam(required = false) Long departmentId,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size
//    ) {
//        List<Student> students;
//
//        if (departmentId != null) {
//            students = studentService.findByDepartmentId(departmentId, page, size);
//        } else {
//            students = studentService.findAll(page, size);
//        }
//
//        return ApiResponseBuilder.success("Danh sách sinh viên", students, ...);
//    }
}
