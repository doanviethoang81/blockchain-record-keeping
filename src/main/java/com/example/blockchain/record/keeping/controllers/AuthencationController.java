package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.dtos.LoginRequest;
import com.example.blockchain.record.keeping.response.LoginResponse;
import com.example.blockchain.record.keeping.dtos.RegisterRequest;
import com.example.blockchain.record.keeping.dtos.request.VerifyOtpRequest;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.*;
import com.example.blockchain.record.keeping.response.ApiResponse;
import com.example.blockchain.record.keeping.response.ApiResponseBuilder;
import com.example.blockchain.record.keeping.services.*;
import com.example.blockchain.record.keeping.utils.JWTUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthencationController {

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
    private final ImageLogoService imageLogoService;


    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @ModelAttribute RegisterRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ApiResponseBuilder.badRequest("Vui lòng nhập đúng đầy đủ thông tin và đúng định dạng!");
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
                    return ApiResponseBuilder.badRequest("File tải lên không phải là ảnh!");
                }
                String imageUrl = imageLogoService.uploadImage(request.getLogo());
                university.setLogo(imageUrl);
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
            user.setCreatedAt(vietnamTime.toLocalDateTime());
            user.setUpdatedAt(vietnamTime.toLocalDateTime());
            userRepository.save(user);

            //tạo random 6 số
            String otp = String.format("%06d", new Random().nextInt(999999));
            otpService.saveOtp(request.getEmail(), otp);
            brevoApiEmailService.sendActivationEmail(request.getEmail(), otp);
            return ApiResponseBuilder.success("Đăng ký thành công! Vui lòng kiểm tra email để kích hoạt tài khoản", null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Đã xảy ra lỗi trong quá trình đăng ký");
        }
    }


    @PostMapping("/login")
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
            if (user.isLocked()) {
                return ApiResponseBuilder.forbidden("Tài khoản của bạn đã bị khóa!");
            }

            Optional<University> university= universityRepository.findByEmail(user.getEmail());
            String role = user.getRole().getName();
            List<UserPermission> userPermissions = userPermissionRepository.findByUserId(user.getId());
            List<String> authorities = userPermissions.stream()
                    .map(up -> up.getPermission().getName())
                    .collect(Collectors.toList());

            if( authorities == null || authorities.isEmpty()){
                return ApiResponseBuilder.forbidden("Tài khoản chưa xác thực!");
            }

            String token = jwtUtil.generateToken(user.getEmail(), List.of(role), authorities);// Tạo JWT token từ email và roles

            String redirectUrl;
            switch (role) {
                case "ADMIN" -> redirectUrl = "/admin/dashboard";
                case "PDT" -> redirectUrl = "/pdt/dashboard";
                case "KHOA" -> redirectUrl = "/khoa/dashboard";
                default -> redirectUrl = "/error";
            }

            LoginResponse response = new LoginResponse(
                    university.get().getName(),
                    user.getEmail(),
                    token,
                    role,
                    redirectUrl,
                    authorities
            );
            return ApiResponseBuilder.success("Đăng nhập thành công!", response);
        } catch (Exception e) {
            return ApiResponseBuilder.unauthorized("Email hoặc mật khẩu không đúng!");
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyOtpRequest request) {
        String email = request.getEmail();
        String otp = request.getOtp();

        boolean valid = otpService.verifyOtp(email, otp);
        if(valid){
            User user = userService.findByUser(email);
            List<Permission> allPermissions = permissionService.listPermission();
            for (Permission permission : allPermissions) {
                UserPermission userPermission = new UserPermission();
                userPermission.setUser(user);
                userPermission.setPermission(permission);
                userPermissionService.save(userPermission);
            }
            return ApiResponseBuilder.success("OTP hợp lệ!", null);
        }
        else{
            return ApiResponseBuilder.badRequest("OTP sai hoặc đã hết hạn");
        }
    }
}
