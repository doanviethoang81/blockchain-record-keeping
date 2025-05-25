package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.dtos.request.ChangePasswordDepartmentRequest;
import com.example.blockchain.record.keeping.dtos.request.DepartmentRequest;
import com.example.blockchain.record.keeping.dtos.request.UserDepartmentRequest;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.response.ApiResponseBuilder;
import com.example.blockchain.record.keeping.response.PaginatedData;
import com.example.blockchain.record.keeping.response.PaginationMeta;
import com.example.blockchain.record.keeping.response.UserReponse;
import com.example.blockchain.record.keeping.services.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
@SecurityRequirement(name = "bearerAuth")
public class DepartmentController {

    private final PasswordEncoder passwordEncoder;
    private final UniversityService universityService;
    private final UserService userService;
    private final DepartmentService departmentService;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final UserPermissionService userPermissionService;

    //---------------------------- PDT -------------------------------------------------------
    //các khoa của trường đại học
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/list-department-of-university")
    public ResponseEntity<?> getListUserOfUniversity(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        try{
            if (page < 1) page = 1;
            if (size < 1) size = 10;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            University university = universityService.getUniversityByEmail(username);

            User currentUser = userService.findByUser(username);
            List<User> listUser = userService.listUser(university).stream()
                    .filter(user -> user.getDepartment() != null) // chỉ lấy user có khoa
                    .filter(user -> !user.getId().equals(currentUser.getId())) // loại bỏ user đang đăng nhập
                    .filter(user -> user.getDepartment().getStatus() != Status.DELETED)
                    .collect(Collectors.toList());
            List<UserReponse> userReponses = new ArrayList<>();

            for (User user : listUser) {
                List<UserPermission> userPermissions = userPermissionService.listUserPermissions(user);

                List<String> permissions = userPermissions.stream()
                        .map(up -> up.getPermission().getAction())
                        .collect(Collectors.toList());

                UserReponse userReponse = new UserReponse(
                        user.getId(),
                        user.getDepartment().getName(),
                        user.getEmail(),
                        user.isLocked(),
                        permissions
                );
                userReponses.add(userReponse);
            }

            int start = (page-1) * size;
            int end = Math.min(start + size, userReponses.size());
            if (start >= userReponses.size()) {
                return ApiResponseBuilder.success("Chưa có khoa nào", null);
            }

            List<UserReponse> pagedResult = userReponses.subList(start, end);
            PaginatedData<UserReponse> data = new PaginatedData<>(pagedResult,
                    new PaginationMeta(userReponses.size(), pagedResult.size(), size, page,
                            (int) Math.ceil((double) userReponses.size() / size)));

            return ApiResponseBuilder.success(
                    "Lấy danh sách các khoa của trường thành công.",data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //tạo khoa
    @PreAuthorize("hasAuthority('WRITE')")
    @PostMapping("/pdt/create-user")
    public ResponseEntity<?> createDepartment(@RequestBody UserDepartmentRequest request) {
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
            department.setStatus(Status.ACTIVE);
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

    //cap lai mk cho khoa xem lại sau nhe
    @PreAuthorize("hasAuthority('WRITE')")
    @PutMapping("/pdt/change-password-of-department")
    public ResponseEntity<?> changePasswordDerpartment(
            @RequestBody ChangePasswordDepartmentRequest changePassword
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            if (changePassword == null ||
                    !StringUtils.hasText(changePassword.getNewPassword())) {
                return ApiResponseBuilder.badRequest("Vui lòng nhập mật khẩu mới cho khoa!");
            }
            boolean isPasswordChanged = userService.changePasswordDepartment(changePassword);
            if (isPasswordChanged) {
                return ApiResponseBuilder.success("Mật khẩu đã được thay đổi thành công.",null);
            } else {
                return ApiResponseBuilder.badRequest("Thay đổi mật khẩu thất bại!");
            }
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    //cấp quyền thu hồi quyền
    @PreAuthorize("hasAuthority('WRITE')")
    @PutMapping("/pdt/open-lock-department/{id}")
    public ResponseEntity<?> lockedDepartment(@PathVariable Long id){
        try{
            User user =userService.finbById(id);
            boolean newLockStatus = !user.isLocked();
            userService.updateLocked(id);
            String message = newLockStatus ? "Khóa tài khoản khoa thành công" : "Mở khóa tài khoản khoa thành công";
            return ApiResponseBuilder.success(message, null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi");
        }
    }

    // mở khóa quyền write của khoa
    @PreAuthorize("hasAuthority('WRITE')")
    @PutMapping("/pdt/unlock-permission-write/{id}")
    public ResponseEntity<?> unlockPermissionWrite(@PathVariable Long id){
        try {
            String active = "WRITE";
            boolean granted = userService.togglePermission(id,active);
            String message = granted ? "Đã cấp quyền WRITE cho khoa" : "Đã thu hồi quyền WRITE của khoa";
            return ApiResponseBuilder.success(message, null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    // mở khóa quyền read của khoa
    @PreAuthorize("hasAuthority('WRITE')")
    @PutMapping("/pdt/unlock-permission-read/{id}")
    public ResponseEntity<?> unlockPermissionRead(@PathVariable Long id){
        try {
            String active = "READ";
            boolean granted = userService.togglePermission(id,active);
            String message = granted ? "Đã cấp quyền READ cho khoa" : "Đã thu hồi quyền READ của khoa";
            return ApiResponseBuilder.success(message, null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    //update khoa
    @PreAuthorize("hasAuthority('WRITE')")
    @PutMapping("/pdt/update-department/{id}")
    public ResponseEntity<?> updateDepartment(@PathVariable Long id, @RequestBody DepartmentRequest departmentRequest){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            University university = universityService.getUniversityByEmail(username);

            if (departmentService.existsByNameAndUniversity(departmentRequest.getName(), university.getId())) {
                return ApiResponseBuilder.badRequest("Tên khoa đã tồn tại trong trường này!");
            }
            if (userService.isEmailRegistered(departmentRequest.getEmail())) {
                return ApiResponseBuilder.badRequest("Email này đã được đăng ký!");
            }

            departmentService.updateDepartment(id,departmentRequest.getName(), departmentRequest.getEmail());
            return ApiResponseBuilder.success("Cập nhật thông tin khoa thành công", null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    //xóa khoa
    @PreAuthorize("hasAuthority('WRITE')")
    @DeleteMapping("/pdt/delete-department/{id}")
    public ResponseEntity<?> deleteDepartment(@PathVariable Long id){
        try {
            departmentService.deleteDepartment(id);
            return ApiResponseBuilder.success("Xóa khoa thành công", null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Đã xảy ra lỗi: " + e.getMessage());
        }
    }

}
