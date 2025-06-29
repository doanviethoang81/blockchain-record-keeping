package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.dtos.LoginRequest;
import com.example.blockchain.record.keeping.dtos.request.ForgotPasswordRequest;
import com.example.blockchain.record.keeping.dtos.request.NewPasswordRequest;
import com.example.blockchain.record.keeping.response.LoginResponse;
import com.example.blockchain.record.keeping.dtos.RegisterRequest;
import com.example.blockchain.record.keeping.dtos.request.VerifyOtpRequest;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.*;
import com.example.blockchain.record.keeping.response.ApiResponse;
import com.example.blockchain.record.keeping.response.ApiResponseBuilder;
import com.example.blockchain.record.keeping.services.*;
import com.example.blockchain.record.keeping.utils.JWTUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class AuthenticationController {

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UniversityRepository universityRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionService permissionService;
    private final UserPermissionService userPermissionService;
    private final UserPermissionRepository userPermissionRepository;
    private final JWTUtil jwtUtil;
    private final OtpService otpService;
    private final BrevoApiEmailService brevoApiEmailService;
    private final UserService userService;
    private final ImageUploadService imageUploadService;
    private final DepartmentService departmentService;
    private final TokenBlacklistService tokenBlacklistService;
    private final UniversityService universityService;
    private final StudentService studentService;

    @PostMapping("/api/auth/register")
    public ResponseEntity<?> register(@Valid @ModelAttribute RegisterRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // Lấy danh sách lỗi và trả về
            List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                    .collect(Collectors.toList());
            return ApiResponseBuilder.listBadRequest("Dữ liệu không hợp lệ!", errors);
        }
        if (request.getLogo() == null || request.getLogo().isEmpty()) {
            return ApiResponseBuilder.badRequest("Logo không được để trống!");
        }
        if (request.getSealImageUrl() == null || request.getSealImageUrl().isEmpty()) {
            return ApiResponseBuilder.badRequest("Dấu mộc không được để trống!");
        }
        try{
            ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

            Optional<University> existingUniversity = universityRepository.findByEmail(request.getEmail());
            if (existingUniversity.isPresent()) {
                return ApiResponseBuilder.badRequest("Email đã được đăng ký!");
            }
            String normalizedName = request.getName().trim().toLowerCase();
            List<University> universities = universityRepository.findAll();
            boolean nameExists = universities.stream()
                    .anyMatch(u -> u.getName() != null && u.getName().trim().toLowerCase().equals(normalizedName));
            if (nameExists) {
                return ApiResponseBuilder.badRequest("Tên trường đã tồn tại!");
            }

            // Tạo user mới
            University university = new University();
            university.setName(request.getName());
            university.setAddress(request.getAddress());
            university.setEmail(request.getEmail());
            university.setTaxCode(request.getTaxCode());
            university.setWebsite(request.getWebsite());
            if (request.getLogo() != null && !request.getLogo().isEmpty()) {
                String contentType = request.getLogo().getContentType();
                if (!contentType.startsWith("image/")) {
                    return ApiResponseBuilder.badRequest("Logo tải lên không phải là ảnh!");
                }
                String imageUrl = imageUploadService.uploadImage(request.getLogo());
                university.setLogo(imageUrl);
            }

            if (request.getSealImageUrl() != null && !request.getSealImageUrl().isEmpty()) {
                String contentType = request.getSealImageUrl().getContentType();
                if (!contentType.startsWith("image/")) {
                    return ApiResponseBuilder.badRequest("Dấu mộc tải lên không phải là ảnh!");
                }
                String sealImageUrl = imageUploadService.uploadImage(request.getSealImageUrl());
                university.setSealImageUrl(sealImageUrl);
            }

            university.setCreatedAt(vietnamTime.toLocalDateTime());
            university.setUpdatedAt(vietnamTime.toLocalDateTime());
            universityRepository.save(university);

            Optional<Role> userRole = roleRepository.findByName("PDT");

            User user = new User();
            user.setRole(userRole.get());
            user.setDepartment(null);
            user.setUniversity(university);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEmail(request.getEmail());
            user.setLocked(false);
            user.setVerified(false);
            user.setCreatedAt(vietnamTime.toLocalDateTime());
            user.setUpdatedAt(vietnamTime.toLocalDateTime());
            userRepository.save(user);

            //tạo random 6 số
            String otp = String.format("%06d", new Random().nextInt(999999));
            otpService.saveOtp(request.getEmail(), otp);
            brevoApiEmailService.sendActivationEmail(request.getEmail(), otp);
            return ApiResponseBuilder.success("Đăng ký thành công, Vui lòng kiểm tra email để kích hoạt tài khoản", null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Đã xảy ra lỗi trong quá trình đăng ký!");
        }
    }

    //không đc để trống request
    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername();

            Optional<User> optionalUser =userRepository.findByEmail(email);
            User user = optionalUser.get();

            Optional<University> university= universityRepository.findByEmail(user.getEmail());
            String role = user.getRole().getName();
            List<UserPermission> userPermissions = userPermissionRepository.findByUserId(user.getId());
            List<String> authorities = userPermissions.stream()
                    .map(up -> up.getPermission().getAction())
                    .collect(Collectors.toList());

            if (!user.isVerified()) {
                String otp = String.format("%06d", new Random().nextInt(999999));
                otpService.saveOtp(request.getEmail(), otp);
                brevoApiEmailService.sendActivationEmail(request.getEmail(), otp);
                return ApiResponseBuilder.forbidden("Tài khoản của bạn chưa được xác minh. Vui lòng xác minh! Mã OTP: " + otp);
            }

            if (user.isLocked()) {
                return ApiResponseBuilder.forbidden("Tài khoản của bạn đã bị khóa!");
            }

            if (authorities.isEmpty()) {
                return ApiResponseBuilder.forbidden("Tài khoản của bạn không có quyền vào hệ thống!");
            }

            String token = jwtUtil.generateToken(user.getEmail(), List.of(role), authorities);

            String redirectUrl;
            switch (role) {
                case "ADMIN" -> redirectUrl = "/admin/dashboard";
                case "PDT" -> redirectUrl = "/pdt/dashboard";
                case "KHOA" -> redirectUrl = "/khoa/dashboard";
                default -> redirectUrl = "/error";
            }
            LoginResponse response;
            if(!university.isEmpty()){
                 response = new LoginResponse(
                        university.get().getName(),
                        user.getEmail(),
                        token,
                        role,
                        redirectUrl,
                        authorities
                );
            }
            else{
                Department department = departmentService.findById(user.getDepartment().getId());
                 response = new LoginResponse(
                        department.getName(),
                        user.getEmail(),
                        token,
                        role,
                        redirectUrl,
                        authorities
                );
            }
            return ApiResponseBuilder.success("Đăng nhập thành công!", response);
        } catch (Exception e) {
            return ApiResponseBuilder.unauthorized("Email hoặc mật khẩu không đúng!");
        }
    }

    // gửi lại otp lúc tạo tài khoản
    @PostMapping("/api/auth/resend-registration-otp")
    public ResponseEntity<?> resendOtp(@RequestBody ForgotPasswordRequest request) {
        try {
            if(request.getEmail()== null || !StringUtils.hasText(request.getEmail())){
                return ApiResponseBuilder.badRequest("Vui lòng nhập email!");
            }
            User user = userService.findByUser(request.getEmail());
            if (user == null) {
                return ApiResponseBuilder.badRequest("Không tìm thấy tài khoản với email này!");
            }
            if(user.isLocked()){
                return ApiResponseBuilder.badRequest("Tài khoản này đã bị khóa!");
            }
            String otp = String.format("%06d", new Random().nextInt(999999));
            otpService.saveOtp(request.getEmail(), otp);
            brevoApiEmailService.sendActivationEmail(request.getEmail(), otp);
            return ApiResponseBuilder.success("Mã OTP đã được gửi tới email", null);
        } catch (Exception e) {
            return ApiResponseBuilder.badRequest(e.getMessage());
        }
    }

    @PostMapping("/api/auth/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) throws NoSuchAlgorithmException {
        String email = request.getEmail();
        String otp = request.getOtp();

        boolean valid = otpService.verifyOtp(email, otp);
        if(valid){
            User user = userService.findByUser(email);
            user.setVerified(true);
            userService.save(user);
            List<Permission> allPermissions = permissionService.listPermission();
            for (Permission permission : allPermissions) {
                UserPermission userPermission = new UserPermission();
                userPermission.setUser(user);
                userPermission.setPermission(permission);
                userPermissionService.save(userPermission);
            }

            // tạo cặp khóa private key, public
            ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            RSAKeyPairGenerator keyGen = new RSAKeyPairGenerator();
            String publicKey = Base64.getEncoder().encodeToString(keyGen.getPublicKey().getEncoded());
            String privateKey = Base64.getEncoder().encodeToString(keyGen.getPrivateKey().getEncoded());
            University university = universityService.findById(user.getUniversity().getId());
            university.setPublicKey(publicKey);
            university.setPrivateKey(privateKey);
            university.setUpdatedAt(vietnamTime.toLocalDateTime());
            universityRepository.save(university);

            return ApiResponseBuilder.success("OTP hợp lệ!", null);
        }
        else{
            return ApiResponseBuilder.badRequest("OTP sai hoặc đã hết hạn!");
        }
    }

    @PostMapping("/api/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ApiResponseBuilder.badRequest("Token không hợp lệ!");
        }
        String token = authHeader.substring(7);
        tokenBlacklistService.blacklistToken(token);

        return ApiResponseBuilder.success("Đăng xuất thành công", null);
    }

    //click quên mật khẩu
    @PostMapping("/api/auth/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            if(request.getEmail()== null || !StringUtils.hasText(request.getEmail())){
                return ApiResponseBuilder.badRequest("Vui lòng nhập email!");
            }
            User user = userService.findByUser(request.getEmail());
            if (user == null) {
                return ApiResponseBuilder.badRequest("Không tìm thấy tài khoản với email này!");
            }
            if(user.isLocked()){
                return ApiResponseBuilder.badRequest("Tài khoản này đã bị khóa!");
            }
            String roleName = user.getRole().getName();
            if (roleName == "KHOA") {
                return ApiResponseBuilder.badRequest("Tài khoản của bạn không có quyền cấp lại mật khẩu!");
            }
            String otp = String.format("%06d", new Random().nextInt(999999));
            otpService.saveOtp(request.getEmail(), otp);
            brevoApiEmailService.sendOtpForgotPasswordEmail(request.getEmail(), otp);
            return ApiResponseBuilder.success("Mã OTP đã được gửi tới email", null);
        } catch (Exception e) {
            return ApiResponseBuilder.badRequest(e.getMessage());
        }
    }

    // xác minh otp quên mật khẩu
    @PostMapping("/api/auth/verify-otp-forgot-password")
    public ResponseEntity<?> verifyOtpFgotPassword(@RequestBody VerifyOtpRequest request) throws NoSuchAlgorithmException {
        if(request.getEmail()== null || !StringUtils.hasText(request.getEmail()) ||
            request.getOtp()== null || !StringUtils.hasText(request.getOtp())
        ){
            return ApiResponseBuilder.badRequest("Vui lòng đầy đủ thông tin!");
        }
        String email = request.getEmail();
        String otp = request.getOtp();
        boolean valid = otpService.verifyOtp(email, otp);
        if(valid){
            return ApiResponseBuilder.success("OTP hợp lệ!", null);
        }
        else{
            return ApiResponseBuilder.badRequest("OTP sai hoặc đã hết hạn!");
        }
    }

    // cấp lại mật khẩu khi quên
    @PostMapping("/api/auth/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody NewPasswordRequest request) throws NoSuchAlgorithmException {
        try {
            String email = request.getEmail();
            String newPassword = request.getNewPassword();
            String confirmPassword = request.getConfirmPassword();

            if(email== null || !StringUtils.hasText(email) ||
                    newPassword == null || !StringUtils.hasText(newPassword) ||
                    confirmPassword == null || !StringUtils.hasText(confirmPassword)
            ){
                return ApiResponseBuilder.badRequest("Vui lòng đầy đủ thông tin!");
            }
            if (!newPassword.equals(confirmPassword)) {
                return ApiResponseBuilder.badRequest("Mật khẩu mới không giống nhau!");
            }
            if (userService.resetPassword(email, newPassword)) {
                return ApiResponseBuilder.success("Cập nhật mật khẩu thành công", null);
            }
            else {
                return ApiResponseBuilder.success("Cật nhật thất bại!", null);
            }
        } catch (Exception e) {
            return ApiResponseBuilder.badRequest(e.getMessage());
        }
    }

    //sinh viên login
    @PostMapping("/api/auth/student-login")
    public ResponseEntity<?> studentLogin(@RequestBody LoginRequest request) {
        try {
            if(request== null || !StringUtils.hasText(request.getEmail())
                || !StringUtils.hasText(request.getPassword())){
                return ApiResponseBuilder.badRequest("Vui lòng nhập đầy đủ thông tin!");
            }
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String email = authentication.getName();

            Student student = studentService.findByEmail(email);
            if (student == null) {
                return ApiResponseBuilder.badRequest("Tài khoản sinh viên không tồn tại!");
            }

            List<String> permissions = List.of("READ");
            String role = "STUDENT";
            String token = jwtUtil.generateToken(student.getEmail(), List.of(role), permissions);
            String redirectUrl = "/student/dashboard";

            LoginResponse response = new LoginResponse(
                    student.getName(),
                    student.getEmail(),
                    token,
                    role,
                    redirectUrl,
                    permissions
            );
            return ApiResponseBuilder.success("Đăng nhập thành công!", response);
        } catch (BadCredentialsException e) {
            return ApiResponseBuilder.badRequest("Email hoặc mật khẩu không đúng!");
        } catch (Exception e) {
            return ApiResponseBuilder.internalError(e.getMessage());
        }
    }

}