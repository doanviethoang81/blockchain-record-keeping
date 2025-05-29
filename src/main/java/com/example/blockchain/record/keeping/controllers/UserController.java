package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.dtos.request.ChangePasswordRequest;
import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.models.User;
import com.example.blockchain.record.keeping.response.ApiResponseBuilder;
import com.example.blockchain.record.keeping.response.DepartmentDetailResponse;
import com.example.blockchain.record.keeping.response.UniversityReponse;
import com.example.blockchain.record.keeping.services.UniversityService;
import com.example.blockchain.record.keeping.services.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
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
    //thông tin chi tiết của tr
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/user-detail")
    public ResponseEntity<?> userDetailUniversity() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            University university = universityService.getUniversityByEmail(username);

            UniversityReponse universityDetailReponse= new UniversityReponse();
            universityDetailReponse.setId(university.getId());
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

    //---------------------------- KHOA -------------------------------------------------------
    //chi tiet tai khoan khoa dang nhap
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/khoa/user-detail")
    public ResponseEntity<?> userDetailDepartment() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);

            UniversityReponse universityDetailReponse= new UniversityReponse();
            universityDetailReponse.setId(user.getUniversity().getId());
            universityDetailReponse.setName(user.getUniversity().getName());
            universityDetailReponse.setEmail(user.getUniversity().getEmail());
            universityDetailReponse.setAddress(user.getUniversity().getAddress());
            universityDetailReponse.setTaxCode(user.getUniversity().getTaxCode());
            universityDetailReponse.setWebsite(user.getUniversity().getWebsite());
            universityDetailReponse.setLogo(user.getUniversity().getLogo());


            DepartmentDetailResponse departmentDetailResponse= new DepartmentDetailResponse();
            departmentDetailResponse.setNameDepartment(user.getDepartment().getName());
            departmentDetailResponse.setUniversityReponse(universityDetailReponse);
            return ApiResponseBuilder.success("Thông tin chi tiết của khoa", departmentDetailResponse);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Đã xảy ra lỗi!");
        }
    }
}
