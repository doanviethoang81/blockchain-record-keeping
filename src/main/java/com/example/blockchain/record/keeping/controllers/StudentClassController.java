package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.response.*;
import com.example.blockchain.record.keeping.services.DepartmentService;
import com.example.blockchain.record.keeping.services.StudentClassService;
import com.example.blockchain.record.keeping.services.UniversityService;
import com.example.blockchain.record.keeping.services.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class StudentClassController {

    private final StudentClassService studentClassService;
    private final UniversityService universityService;
    private final DepartmentService departmentService;
    private final UserService userService;


    //---------------------------- PDT -------------------------------------------------------
    //list lớp của 1 trường vs tìm theo tên lớp
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/list-class-of-university")
    public ResponseEntity<?> getAllClassOfUniversity(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name
    ) {
        try{
            if (page < 1) page = 1;
            if (size < 1) size = 10;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            University university = universityService.getUniversityByEmail(username);
            List<StudentClassReponse> studentClassList = studentClassService.getAllClassofUniversity(university.getId(), name);
            if ((name != null && !name.isEmpty())) {
                if(studentClassList.isEmpty()){
                    return ApiResponseBuilder.success("Không tìm thấy lớp!", null);
                }
            }
            int start = (page - 1) * size;
            int end = Math.min(start + size, studentClassList.size());
            if (start >= studentClassList.size()) {
                return ApiResponseBuilder.success("Trường chưa có lớp nào", null);
            }

            List<StudentClassReponse> pagedResult = studentClassList.subList(start, end);
            PaginatedData<StudentClassReponse> data = new PaginatedData<>(pagedResult,
                    new PaginationMeta(studentClassList.size(), pagedResult.size(), size, page,
                            (int) Math.ceil((double) studentClassList.size() / size)));


            return ApiResponseBuilder.success("Lấy danh sách lớp của trường thành công", data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //tạo lớp
    @PreAuthorize("hasAuthority('WRITE')")
    @PostMapping("/pdt/create-class")
    public ResponseEntity<?> createClassOfDepartment(
            HttpServletRequest request) {
        try{
            ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            String id = request.getParameter("id");
            Long departmentId = Long.parseLong(id);
            String name = request.getParameter("name").trim();

            Department department= departmentService.findById(departmentId);

            if(request == null ||!StringUtils.hasText(name)){
                return ApiResponseBuilder.badRequest("Vui lòng nhập đầy đủ thông tin!");
            }
            if (studentClassService.existsByNameAndDepartmentIdAndStatus(name,department)){
                return ApiResponseBuilder.badRequest("Tên lớp đã tồn tại trong khoa này!");
            }
            StudentClass studentClass= new StudentClass();
            studentClass.setDepartment(department);
            studentClass.setName(name);
            studentClass.setStatus(Status.ACTIVE);
            studentClass.setCreatedAt(vietnamTime.toLocalDateTime());
            studentClass.setUpdatedAt(vietnamTime.toLocalDateTime());

            studentClassService.save(studentClass);

            return ApiResponseBuilder.success("Tạo lớp học thành công", null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //update khoa
    @PreAuthorize("hasAuthority('WRITE')")
    @PutMapping("/pdt/update-class/{id}")
    public ResponseEntity<?> updateClass(@PathVariable Long id, HttpServletRequest request){
        try {
            ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

            String name = request.getParameter("name");

            StudentClass studentClass = studentClassService.findById(id);

            if(!StringUtils.hasText(name)){
                return ApiResponseBuilder.badRequest("Vui lòng nhập đầy đủ thông tin!");
            }
            if (studentClassService.existsByNameAndDepartmentIdAndStatus(name,studentClass.getDepartment())){
                return ApiResponseBuilder.badRequest("Tên lớp đã tồn tại trong khoa này!");
            }

            studentClass.setName(name);
            studentClass.setUpdatedAt(vietnamTime.toLocalDateTime());
            studentClassService.save(studentClass);
            return ApiResponseBuilder.success("Cập nhật thông tin lớp thành công", null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    //xóa khoa
    @PreAuthorize("hasAuthority('WRITE')")
    @DeleteMapping("/pdt/delete-class/{id}")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id){
        try {
            studentClassService.deleteStudentClass(id);
            return ApiResponseBuilder.success("Xóa lớp thành công", null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    //---------------------------- KHOA -------------------------------------------------------
    // ds lớp cho khoa vs tìm theo tên
    @PreAuthorize("hasAuthority('WRITE')")
    @GetMapping("/khoa/list-class-of-department")
    public ResponseEntity<?> searchDepartment(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name){
        try {
            if (page < 1) page = 1;
            if (size < 1) size = 10;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);

            List<StudentClass> studentClassList =
                    studentClassService.findAllClassesByDepartmentId(user.getDepartment().getId(), name);
            if(name!= null && !name.isEmpty()){
                if(studentClassList.isEmpty()){
                    return ApiResponseBuilder.success("Không tìm thấy lớp!", null);
                }
            }

            List<StudentClassReponse> studentClassReponseList= new ArrayList<>();
            for (StudentClass studentClass : studentClassList){
                StudentClassReponse studentClassReponse = new StudentClassReponse(
                        studentClass.getId(),
                        studentClass.getName()
                );
                studentClassReponseList.add(studentClassReponse);
            }

            int start = (page - 1) * size;
            int end = Math.min(start + size, studentClassReponseList.size());
            if (start >= studentClassReponseList.size()) {
                return ApiResponseBuilder.success("Chưa có lớp nào!", null);
            }

            List<StudentClassReponse> pagedResult = studentClassReponseList.subList(start, end);
            PaginatedData<StudentClassReponse> data = new PaginatedData<>(pagedResult,
                    new PaginationMeta(studentClassReponseList.size(), pagedResult.size(), size, page ,
                            (int) Math.ceil((double) studentClassReponseList.size() / size)));

            return ApiResponseBuilder.success("Danh sách lớp của một khoa", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Đã xảy ra lỗi: " + e.getMessage());
        }
    }





}
