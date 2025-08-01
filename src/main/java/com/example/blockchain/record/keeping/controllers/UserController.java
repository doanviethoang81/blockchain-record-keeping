package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.dtos.request.*;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.response.*;
import com.example.blockchain.record.keeping.services.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UniversityService universityService;
    private final UserService userService;
    private final BrevoApiEmailService brevoApiEmailService;
    private final PasswordEncoder passwordEncoder;
    private final StudentService studentService;
    private final WalletService walletService;

    //---------------------------- ADMIN -------------------------------------------------------
    // khóa/Mở tài khoản của 1 trường
    @PreAuthorize("@permissionService.hasPermission(authentication, 'CREATE')")
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
    @PreAuthorize("@permissionService.hasPermission(authentication, 'UPDATE')")
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


    //---------------------------- PDT -------------------------------------------------------
    //thông tin chi tiết của tr
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/user-detail")
    public ResponseEntity<?> userDetailUniversity() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);
            University university = universityService.getUniversityByEmail(username);

            UniversityDetaillResponse universityDetailReponse= new UniversityDetaillResponse(
                    user.getId(),
                    university.getName(),
                    university.getEmail(),
                    university.getAddress(),
                    university.getTaxCode(),
                    university.getWebsite(),
                    university.getPublicKey(),
                    university.getLogo(),
                    university.getSealImageUrl()
            );
            return ApiResponseBuilder.success("Thông tin chi tiết của trường đại học", universityDetailReponse);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Đã xảy ra lỗi!");
        }
    }

    //đổi mật khẩu tài khoản pdt
    @PreAuthorize("@permissionService.hasPermission(authentication, 'UPDATE')")
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

//    // khóa/Mở tài khoản của 1 khoa PDT
//    @PutMapping("/pdt/unlock-department/{id}")
//    public ResponseEntity<?> lockDepartment(@PathVariable("id") Long id) {
//        try {
//            ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
//
//            User user = userService.finbById(id);
//            boolean isLocked = user.isLocked();
//
//            user.setLocked(!isLocked);
//            user.setUpdatedAt(vietnamTime.toLocalDateTime());
//            userService.save(user);
//
//            String actionType = isLocked ? "được mở khóa tài khoản" : "bị khóa tài khoản";
//            brevoApiEmailService.sendPermissionToDepartment(id, actionType);
//
//            String message = isLocked ? "Mở khóa tài khoản thành công!" : "Khóa tài khoản thành công!";
//            return ApiResponseBuilder.success(message, null);
//        } catch (Exception e) {
//            return ApiResponseBuilder.internalError("Lỗi " + e.getMessage());
//        }
//    }

    //chi tiet tai khoan khoa dang nhap
    @GetMapping("/khoa/user-detail")
    public ResponseEntity<?> userDetailDepartment() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);

            UniversityResponse universityDetailReponse= new UniversityResponse();
            universityDetailReponse.setId(user.getUniversity().getId());
            universityDetailReponse.setName(user.getUniversity().getName());
            universityDetailReponse.setEmail(user.getUniversity().getEmail());
            universityDetailReponse.setAddress(user.getUniversity().getAddress());
            universityDetailReponse.setTaxCode(user.getUniversity().getTaxCode());
            universityDetailReponse.setWebsite(user.getUniversity().getWebsite());
            universityDetailReponse.setLogo(user.getUniversity().getLogo());

            DepartmentDetailResponse departmentDetailResponse= new DepartmentDetailResponse();
            departmentDetailResponse.setDepartmentId(user.getId());
            departmentDetailResponse.setNameDepartment(user.getDepartment().getName());
            departmentDetailResponse.setEmail(user.getEmail());
            departmentDetailResponse.setUniversityResponse(universityDetailReponse);
            return ApiResponseBuilder.success("Thông tin chi tiết của khoa", departmentDetailResponse);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Đã xảy ra lỗi!");
        }
    }

    //xác nhận password để xem private key
    @PreAuthorize("hasAuthority('READ')")
    @PostMapping("/pdt/private-key")
    public ResponseEntity<?> verifyPassword(@RequestBody VerifyPasswordRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            if(request == null || !StringUtils.hasText(request.getPassword())){
                return ApiResponseBuilder.badRequest("Vui lòng nhập mật khẩu!");
            }

            User user = userService.findByUser(username);
            if (user == null) {
                return ApiResponseBuilder.badRequest("Không tìm thấy người dùng");
            }

            boolean isPasswordCorrect = passwordEncoder.matches(request.getPassword(), user.getPassword());
            if (!isPasswordCorrect) {
                return ApiResponseBuilder.badRequest("Mật khẩu không đúng");
            }
            PrivateKeyResponse response = new PrivateKeyResponse(
                    user.getUniversity().getPrivateKey()
            );
            return ApiResponseBuilder.success("Xác nhận thành công", response);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Đã xảy ra lỗi!");
        }
    }

    //chi tiet tai khoan sinh viên dang nhap
    @GetMapping("/student/user-detail")
    public ResponseEntity<?> userDetailStudent() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Student student= studentService.findByEmail(username);
            Wallet wallet= walletService.findByStudent(student);
            BigDecimal stuCoin;
            if(wallet == null){
                stuCoin = BigDecimal.valueOf(0);
            }
            else {
                stuCoin = new BigDecimal(wallet.getCoin()).divide(new BigDecimal("1000000000000000000")); // chia 10^18
            }
            StudentDetailResponse studentResponse= new StudentDetailResponse(
                    student.getName(),
                    student.getStudentCode(),
                    student.getEmail(),
                    student.getBirthDate(),
                    student.getCourse(),
                    student.getStudentClass().getId(),
                    student.getStudentClass().getName(),
                    student.getStudentClass().getDepartment().getId(),
                    student.getStudentClass().getDepartment().getName(),
                    student.getStudentClass().getDepartment().getUniversity().getName(),
                    wallet != null ? wallet.getWalletAddress() : null,
                    wallet != null ? wallet.getPublicKey() : null,
                    wallet != null ? wallet.getPrivateKey() : null,
                    stuCoin.toString()
            );
            return ApiResponseBuilder.success("Thông tin chi tiết của sinh viên", studentResponse);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Đã xảy ra lỗi!");
        }
    }

    //sinh viên tự thay đổi mk
    @PutMapping("/student/change-password")
    public ResponseEntity<?> changePasswordStudent(
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
            boolean isPasswordChanged = studentService.changePasswordOfStudent(username, changePasswordRequest);
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
}