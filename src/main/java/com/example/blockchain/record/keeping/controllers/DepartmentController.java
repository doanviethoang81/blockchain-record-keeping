package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.dtos.request.ChangePasswordDepartmentRequest;
import com.example.blockchain.record.keeping.dtos.request.DepartmentRequest;
import com.example.blockchain.record.keeping.dtos.request.UserDepartmentRequest;
import com.example.blockchain.record.keeping.enums.ActionType;
import com.example.blockchain.record.keeping.enums.Entity;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.LogRepository;
import com.example.blockchain.record.keeping.response.ApiResponseBuilder;
import com.example.blockchain.record.keeping.response.PaginatedData;
import com.example.blockchain.record.keeping.response.PaginationMeta;
import com.example.blockchain.record.keeping.response.UserResponse;
import com.example.blockchain.record.keeping.services.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
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
import java.util.List;

@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DepartmentController {

    private final PasswordEncoder passwordEncoder;
    private final UniversityService universityService;
    private final UserService userService;
    private final DepartmentService departmentService;
    private final BrevoApiEmailService brevoApiEmailService;
    private final AuditLogService auditLogService;
    private final HttpServletRequest httpServletRequest;
    private final LogRepository logRepository;

    //---------------------------- PDT -------------------------------------------------------
    //các khoa của trường đại học
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/list-department-of-university")
    public ResponseEntity<?> getListDepartmentOfUniversity(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name
    ){
        try{
            if (page < 1) page = 1;
            if (size < 1) size = 10;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            University university = universityService.getUniversityByEmail(username);

            List<UserResponse> userRepons = userService.getDepartmentDetailOfUniversity(university.getId(),name);

            if (name != null && !name.isEmpty()) {
                if (userRepons.isEmpty()) {
                    return ApiResponseBuilder.success("Không tìm thấy khoa!",userRepons);
                }
            }

            int start = (page-1) * size;
            int end = Math.min(start + size, userRepons.size());
            if (start >= userRepons.size()) {
                return ApiResponseBuilder.success("Chưa có khoa nào!",userRepons);
            }

            List<UserResponse> pagedResult = userRepons.subList(start, end);
            PaginatedData<UserResponse> data = new PaginatedData<>(pagedResult,
                    new PaginationMeta(userRepons.size(), pagedResult.size(), size, page,
                            (int) Math.ceil((double) userRepons.size() / size)));

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
            departmentService.create(request,university);
            return ApiResponseBuilder.success("Tạo tài khoản khoa thành công", null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //cap lai mk cho khoa
    @PreAuthorize("hasAuthority('WRITE')")
    @PutMapping("/pdt/change-password-of-department/{id}")
    public ResponseEntity<?> changePasswordDerpartment(
            @PathVariable("id") Long id,
            @RequestBody ChangePasswordDepartmentRequest changePassword
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User userUniversity = userService.findByUser(username);
            if (changePassword == null ||
                    !StringUtils.hasText(changePassword.getNewPassword())) {
                return ApiResponseBuilder.badRequest("Vui lòng nhập đầy đủi dữ liệu!");
            }
            if(!changePassword.getConfirmPassword().equals(changePassword.getNewPassword())){
                return ApiResponseBuilder.badRequest("Mật khẩu mới không giống nhau!");
            }
            if(!passwordEncoder.matches(changePassword.getPasswordUniversity(), userUniversity.getPassword())){
                return ApiResponseBuilder.badRequest("Mật khẩu của trường không đúng!");
            }
            boolean isPasswordChanged = userService.changePasswordDepartment(id, changePassword);
            if (isPasswordChanged) {
                // gửi gmail thông báo
                brevoApiEmailService.sendPasswordChange(id, changePassword.getNewPassword());
                return ApiResponseBuilder.success("Thay đổi mật khẩu thành công.",null);

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

            //log
            ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            String ipAdress = auditLogService.getClientIp(httpServletRequest);
            Log log = new Log();
            log.setUser(auditLogService.getCurrentUser());
            log.setActionType(newLockStatus ? ActionType.LOCKED : ActionType.UNLOCKED);
            log.setEntityName(Entity.departments);
            log.setEntityId(id);
            log.setDescription((newLockStatus ? ActionType.LOCKED.getLabel() : ActionType.UNLOCKED.getLabel())+ user.getDepartment().getName());
            log.setIpAddress(ipAdress);
            log.setCreatedAt(vietnamTime.toLocalDateTime());
            logRepository.save(log);

            brevoApiEmailService.sendPermissionToDepartment(id, message);
            return ApiResponseBuilder.success(message, null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi "+ e.getMessage());
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
            String actionType = granted ? "được cấp quyền WRITE ":" bị thu hồi quyền WRITE";
            User user =userService.finbById(id);
            //log
            ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            String ipAdress = auditLogService.getClientIp(httpServletRequest);
            Log log = new Log();
            log.setUser(auditLogService.getCurrentUser());
            log.setActionType(granted ? ActionType.UNLOCK_WRITE : ActionType.LOCK_WRITE);
            log.setEntityName(Entity.departments);
            log.setEntityId(id);
            log.setDescription((granted ? ActionType.UNLOCK_WRITE.getLabel() : ActionType.LOCK_WRITE.getLabel()) + " khoa: " + user.getDepartment().getName());
            log.setIpAddress(ipAdress);
            log.setCreatedAt(vietnamTime.toLocalDateTime());
            logRepository.save(log);
            // Gửi email
            brevoApiEmailService.sendPermissionNotification(id, actionType);
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
            String actionType = granted ? "được cấp quyền READ ":" bị thu hồi quyền READ";
            User user =userService.finbById(id);

            //log
            ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            String ipAdress = auditLogService.getClientIp(httpServletRequest);
            Log log = new Log();
            log.setUser(auditLogService.getCurrentUser());
            log.setActionType(granted ? ActionType.UNLOCK_READ : ActionType.LOCK_READ);
            log.setEntityName(Entity.departments);
            log.setEntityId(id);
            log.setDescription((granted ? ActionType.UNLOCK_READ.getLabel() : ActionType.LOCK_READ.getLabel()) + " khoa: " + user.getDepartment().getName());
            log.setIpAddress(ipAdress);
            log.setCreatedAt(vietnamTime.toLocalDateTime());
            logRepository.save(log);
            // Gửi email
            brevoApiEmailService.sendPermissionNotification(id, actionType);
            return ApiResponseBuilder.success(message, null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    //sửa khoa
    @PreAuthorize("hasAuthority('WRITE')")
    @PutMapping("/pdt/update-department/{id}")
    public ResponseEntity<?> updateDepartment(@PathVariable Long id, @RequestBody DepartmentRequest departmentRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            University university = universityService.getUniversityByEmail(username);
            User user = userService.finbById(id);

            // không xét chính nó
            if (departmentService.existsByNameIgnoreCaseAndUniversityIdAndStatusAndIdNot(departmentRequest.getName(), university.getId(), user.getDepartment().getId())) {
                return ApiResponseBuilder.badRequest("Tên khoa đã tồn tại trong trường này!");
            }
            if(!departmentRequest.getEmail().equals( user.getEmail())){
                if (userService.isEmailRegistered( departmentRequest.getEmail())) {
                    return ApiResponseBuilder.badRequest("Email này đã được đăng ký!");
                }
            }
            departmentService.updateDepartment(user.getDepartment().getId(), departmentRequest);
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
            User user = userService.finbById(id);
            departmentService.deleteDepartment(user.getDepartment().getId());
            return ApiResponseBuilder.success("Xóa khoa thành công", null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Đã xảy ra lỗi: " + e.getMessage());
        }
    }

    // xem chi tiết các khoa 1 trường đh
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/admin/list-department-of-university/{id}")
    public ResponseEntity<?> getListDepartmentOfUniversityAdmin(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name
    ){
        try{
            if (page < 1) page = 1;
            if (size < 1) size = 10;
            User user = userService.finbById(id);

            List<UserResponse> userRepons = userService.getDepartmentDetailOfUniversity(user.getUniversity().getId(),name);

            if (name != null && !name.isEmpty()) {
                if (userRepons.isEmpty()) {
                    return ApiResponseBuilder.success("Không tìm thấy khoa!",userRepons);
                }
            }

            int start = (page-1) * size;
            int end = Math.min(start + size, userRepons.size());
            if (start >= userRepons.size()) {
                return ApiResponseBuilder.success("Chưa có khoa nào!",userRepons);
            }

            List<UserResponse> pagedResult = userRepons.subList(start, end);
            PaginatedData<UserResponse> data = new PaginatedData<>(pagedResult,
                    new PaginationMeta(userRepons.size(), pagedResult.size(), size, page,
                            (int) Math.ceil((double) userRepons.size() / size)));

            return ApiResponseBuilder.success(
                    "Lấy danh sách các khoa của trường thành công.",data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}