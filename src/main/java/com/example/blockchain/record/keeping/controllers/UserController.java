package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.dtos.request.ChangePasswordDepartmentRequest;
import com.example.blockchain.record.keeping.dtos.request.ChangePasswordRequest;
import com.example.blockchain.record.keeping.dtos.request.UserKhoaRequest;
import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.models.User;
import com.example.blockchain.record.keeping.response.ApiResponseBuilder;
import com.example.blockchain.record.keeping.response.DepartmentDetailResponse;
import com.example.blockchain.record.keeping.response.UniversityDetailResponse;
import com.example.blockchain.record.keeping.services.UniversityService;
import com.example.blockchain.record.keeping.services.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UniversityService universityService;
    private final UserService userService;

    //---------------------------- ADMIN -------------------------------------------------------
    //---------------------------- PDT -------------------------------------------------------
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/user-detail")
    public ResponseEntity<?> userDetailUniversity() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            University university = universityService.getUniversityByEmail(username);

            UniversityDetailResponse universityDetailReponse= new UniversityDetailResponse();
            universityDetailReponse.setName(university.getName());
            universityDetailReponse.setEmail(university.getEmail());
            universityDetailReponse.setAddress(university.getAddress());
            universityDetailReponse.setTaxCode(university.getTaxCode());
            universityDetailReponse.setWebsite(university.getWebsite());
            universityDetailReponse.setLogo(university.getLogo());
            return ApiResponseBuilder.success("Thông tin chi tiết của trường đại học", universityDetailReponse);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Đã xảy ra lỗi!");
        }
    }

    //đổi mật khẩu tài khoản pdt
    @PreAuthorize("hasAuthority('WRITE')")
    @PutMapping("/pdt/change-password")
    public ResponseEntity<?> changePasswordUniversity(
            @RequestBody ChangePasswordRequest changePasswordRequest
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            if (changePasswordRequest == null || !StringUtils.hasText(changePasswordRequest.getOldPassword()) ||
                    !StringUtils.hasText(changePasswordRequest.getNewPassword()) || !StringUtils.hasText(changePasswordRequest.getConfirmPassword())) {
                return ApiResponseBuilder.badRequest("Vui lòng nhập đầy đủ thông tin!");
            }

            boolean isPasswordChanged = userService.changePassword(username, changePasswordRequest);
            if (isPasswordChanged) {
                return ApiResponseBuilder.success("Mật khẩu đã được thay đổi thành công.",null);
            } else if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
                return ApiResponseBuilder.badRequest("Mật khẩu mới không giống nhau!");
            } else {
                return ApiResponseBuilder.badRequest("Mật khẩu cũ không chính xác!");
            }
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
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


    //---------------------------- KHOA -------------------------------------------------------
    //chi tiet tai khoan khoa dang nhap
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/khoa/user-detail")
    public ResponseEntity<?> userDetailDepartment() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);

            UniversityDetailResponse universityDetailReponse= new UniversityDetailResponse();
            universityDetailReponse.setName(user.getUniversity().getName());
            universityDetailReponse.setEmail(user.getUniversity().getEmail());
            universityDetailReponse.setAddress(user.getUniversity().getAddress());
            universityDetailReponse.setTaxCode(user.getUniversity().getTaxCode());
            universityDetailReponse.setWebsite(user.getUniversity().getWebsite());
            universityDetailReponse.setLogo(user.getUniversity().getLogo());


            DepartmentDetailResponse departmentDetailResponse= new DepartmentDetailResponse();
            departmentDetailResponse.setNameDepartment(user.getDepartment().getName());
            departmentDetailResponse.setUniversityDetailResponse(universityDetailReponse);
            return ApiResponseBuilder.success("Thông tin chi tiết của khoa", departmentDetailResponse);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Đã xảy ra lỗi!");
        }
    }
}
