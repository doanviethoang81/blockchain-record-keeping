package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.dtos.CertificateTypeDTO;
import com.example.blockchain.record.keeping.dtos.request.UserKhoaRequest;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.PermissionRepository;
import com.example.blockchain.record.keeping.response.*;
import com.example.blockchain.record.keeping.services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
public class UniversityController {
    private final PasswordEncoder passwordEncoder;
    private final UniversityService universityService;
    private final UserService userService;
    private final DepartmentService departmentService;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final UserPermissionService userPermissionService;

//---------------------------- ADMIN -------------------------------------------------------
//---------------------------- PDT -------------------------------------------------------
    //tạo khoa
    @PreAuthorize("hasAuthority('WRITE')")
    @PostMapping("/pdt/create-user")
    public ResponseEntity<?> verifyOtp(@RequestBody UserKhoaRequest request) {
        try{
            ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

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
            if (userService.isEmailRegistered(request.getEmail())) {
                return ApiResponseBuilder.badRequest("Email này đã được đăng ký!");
            }
            Role role =roleService.findByName("KHOA");

            Department department = new Department();
            department.setName(request.getName());
            department.setUniversity(university);
            department.setCreatedAt(vietnamTime.toLocalDateTime());
            department.setUpdatedAt(vietnamTime.toLocalDateTime());
            departmentService.save(department);

            User user = new User();
            user.setUniversity(university);
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRole(role);
            user.setDepartment(department);
            user.setCreatedAt(vietnamTime.toLocalDateTime());
            user.setUpdatedAt(vietnamTime.toLocalDateTime());

            userService.save(user);

            List<Permission> allPermissions = permissionService.listPermission();
            for (Permission permission : allPermissions) {
                UserPermission userPermission = new UserPermission();
                userPermission.setUser(user);
                userPermission.setPermission(permission);
                userPermissionService.save(userPermission);
            }
            return ApiResponseBuilder.success("Tạo tài khoản khoa thành công", null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //các khoa của trường đại học
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/user")
    public ResponseEntity<?> getListUserOfUniversity(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            University university = universityService.getUniversityByEmail(username);

            User currentUser = userService.findByUser(username);
            List<User> listUser = userService.listUser(university).stream()
                    .filter(user -> user.getDepartment() != null) // chỉ lấy user có khoa
                    .filter(user -> !user.getId().equals(currentUser.getId())) // loại bỏ user đang đăng nhập
                    .collect(Collectors.toList());
            List<UserReponse> userReponses = new ArrayList<>();

            for (User user : listUser) {
                List<UserPermission> userPermissions = userPermissionService.listUserPermissions(user);

                List<String> permissions = userPermissions.stream()
                        .map(up -> up.getPermission().getName())
                        .collect(Collectors.toList());

                UserReponse userReponse = new UserReponse(
                        user.getId(),
                        user.getDepartment().getName(),
                        user.getEmail(),
                        permissions
                );
                userReponses.add(userReponse);
            }

            int start = page * size;
            int end = Math.min(start + size, userReponses.size());
            if (start >= userReponses.size()) {
                return ApiResponseBuilder.success("Chưa có khoa nào", null);
            }

            List<UserReponse> pagedResult = userReponses.subList(start, end);
            PaginatedData<UserReponse> data = new PaginatedData<>(pagedResult,
                    new PaginationMeta(userReponses.size(), pagedResult.size(), size, page + 1,
                            (int) Math.ceil((double) userReponses.size() / size)));

            return ApiResponseBuilder.success(
                    "Lấy danh sách các khoa của trường thành công.",data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
