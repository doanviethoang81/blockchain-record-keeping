package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.dtos.StatisticsAdminDTO;
import com.example.blockchain.record.keeping.dtos.StatisticsDepartmentDTO;
import com.example.blockchain.record.keeping.dtos.StatisticsUniversityDTO;
import com.example.blockchain.record.keeping.dtos.request.ChangePasswordRequest;
import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.models.User;
import com.example.blockchain.record.keeping.response.ApiResponseBuilder;
import com.example.blockchain.record.keeping.response.DepartmentDetailResponse;
import com.example.blockchain.record.keeping.response.UniversityReponse;
import com.example.blockchain.record.keeping.services.BrevoApiEmailService;
import com.example.blockchain.record.keeping.services.DepartmentService;
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

import java.time.ZoneId;
import java.time.ZonedDateTime;

@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UniversityService universityService;
    private final UserService userService;
    private final BrevoApiEmailService brevoApiEmailService;
    private final DepartmentService departmentService;

    //---------------------------- ADMIN -------------------------------------------------------
    // khóa/Mở tài khoản của 1 trường
    @PutMapping("/admin/unlock-university/{id}")
    public ResponseEntity<?> lockUniversity(@PathVariable("id") Long id) {
        try {
            ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

            User user = userService.finbById(id);
            boolean isLocked = user.isLocked();

            user.setLocked(!isLocked);
            user.setUpdatedAt(vietnamTime.toLocalDateTime());
            userService.save(user);

            String actionType = isLocked ? "được mở khóa tài khoản" : "bị khóa tài khoản";
            brevoApiEmailService.sendNoticeToUnniversity(id, actionType);

            String message = isLocked ? "Mở khóa tài khoản thành công!" : "Khóa tài khoản thành công!";
            return ApiResponseBuilder.success(message, null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi " + e.getMessage());
        }
    }

    //đổi mật khẩu tài khoản admin
    @PreAuthorize("hasAuthority('WRITE')")
    @PutMapping("/admin/change-password")
    public ResponseEntity<?> changePasswordAdmin(
            @RequestBody ChangePasswordRequest changePasswordRequest
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            if (changePasswordRequest == null || !StringUtils.hasText(changePasswordRequest.getOldPassword()) ||
                    !StringUtils.hasText(changePasswordRequest.getNewPassword()) || !StringUtils.hasText(changePasswordRequest.getConfirmPassword())) {
                return ApiResponseBuilder.badRequest("Vui lòng nhập đầy đủ thông tin!");
            }
            if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
                return ApiResponseBuilder.badRequest("Mật khẩu mới không giống nhau!");
            }
            boolean isPasswordChanged = userService.changePassword(username, changePasswordRequest);
            if (isPasswordChanged) {
                return ApiResponseBuilder.success("Mật khẩu đã được thay đổi thành công.",null);
            }
            else {
                return ApiResponseBuilder.badRequest("Mật khẩu cũ không chính xác!");
            }
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    //    Dashboard admin theem truong bi khoa
    @GetMapping("/admin/dashboard")
    public ResponseEntity<?> getDashboardAdmin(){
        try{
            StatisticsAdminDTO statisticsAdminDTO = userService.dashboardAdmin();
            return ApiResponseBuilder.success("Lấy thông tin dashboard admin thành công", statisticsAdminDTO);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi " + e.getMessage());
        }
    }

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

    // khóa/Mở tài khoản của 1 khoa PDT
    @PutMapping("/pdt/unlock-department/{id}")
    public ResponseEntity<?> lockDepartment(@PathVariable("id") Long id) {
        try {
            ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

            User user = userService.finbById(id);
            boolean isLocked = user.isLocked();

            user.setLocked(!isLocked);
            user.setUpdatedAt(vietnamTime.toLocalDateTime());
            userService.save(user);

            String actionType = isLocked ? "được mở khóa tài khoản" : "bị khóa tài khoản";
            brevoApiEmailService.sendPermissionToDepartment(id, actionType);

            String message = isLocked ? "Mở khóa tài khoản thành công!" : "Khóa tài khoản thành công!";
            return ApiResponseBuilder.success(message, null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi " + e.getMessage());
        }
    }

    //    Dashboard pdt
    @GetMapping("/pdt/dashboard")
    public ResponseEntity<?> getDashboardUniversity(){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            University university = universityService.getUniversityByEmail(username);

            StatisticsUniversityDTO statisticsUniversityDTO = userService.dashboardUniversity(university.getId());
            return ApiResponseBuilder.success("Lấy thông tin dashboard university thành công", statisticsUniversityDTO);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi " + e.getMessage());
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

    //    Dashboard khoa
    @GetMapping("/khoa/dashboard")
    public ResponseEntity<?> getDashboardDepartment(){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);

            StatisticsDepartmentDTO statisticsDepartmentDTO = userService.dashboarDepartment(user.getDepartment().getId());
            return ApiResponseBuilder.success("Lấy thông tin dashboard khoa thành công", statisticsDepartmentDTO);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi " + e.getMessage());
        }
    }
}
