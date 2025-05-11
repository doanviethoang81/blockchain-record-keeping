package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.dtos.request.UserKhoaRequest;
import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.Role;
import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.models.User;
import com.example.blockchain.record.keeping.response.ApiResponseBuilder;
import com.example.blockchain.record.keeping.services.DepartmentService;
import com.example.blockchain.record.keeping.services.RoleService;
import com.example.blockchain.record.keeping.services.UniversityService;
import com.example.blockchain.record.keeping.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
public class UniversityController {
    private final PasswordEncoder passwordEncoder;
    private final UniversityService universityService;
    private final UserService userService;
    private final DepartmentService departmentService;
    private final RoleService roleService;

//---------------------------- ADMIN -------------------------------------------------------
    @PreAuthorize("hasAuthority('WRITE')")
    @PostMapping("/pdt/create-user")
    public ResponseEntity<?> verifyOtp(@RequestBody UserKhoaRequest request) {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            University university = universityService.getUniversityByEmail(username);

            if (request == null || !StringUtils.hasText(request.getName()) ||
                    !StringUtils.hasText(request.getEmail()) || !StringUtils.hasText(request.getPassword())) {
                return ApiResponseBuilder.badRequest("Vui lòng nhập đầy đủ thông tin khoa!");
            }
            if (departmentService.existsByNameAndUniversity(request.getName(), university.getId())) {
                return ApiResponseBuilder.badRequest("Tên khoa đã tồn tại trong trường này!");
            }
            Role role =roleService.findByName("KHOA");

            Department department = new Department();
            department.setName(request.getName());
            department.setUniversity(university);
            departmentService.save(department);

            User user = new User();
            user.setUniversity(university);
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(role);
            user.setDepartment(department);

            userService.save(user);
            return ApiResponseBuilder.success("Tạo tài khoản khoa thành công", null,null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
