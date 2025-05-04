package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.dtos.LoginRequest;
import com.example.blockchain.record.keeping.dtos.LoginResponse;
import com.example.blockchain.record.keeping.dtos.RegisterRequest;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.*;
import com.example.blockchain.record.keeping.utils.JWTUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthencationController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    private final UniversityRepository universityRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final JWTUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Vui lòng nhập đúng đầy đủ thông tin!");
        }
        try{
            Optional<University> existingUniversity = universityRepository.findByEmail(request.getEmail());
            if (existingUniversity.isPresent()) {
                // Nếu email đã tồn tại
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email đã được đăng ký!");
            }

            //tạo random 6 số
            LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(10);  // Mã có hiệu lực trong 10 phút plusMinutes
            // Tạo user mới
            University university = new University();
            university.setName(request.getName());
            university.setAddress(request.getAddress());
            university.setEmail(request.getEmail());
            university.setTaxCode(request.getTaxCode()); // Ban đầu tài khoản chưa kích hoạt
            university.setWebsite(request.getWebsite()); // Gán role cho user
            university.setLogo(request.getLogo());
            universityRepository.save(university);

            Optional<Role> userRole = roleRepository.findByName("PDT");

            User user = new User();
            user.setRole(userRole.get());
            user.setDepartment(null);
            user.setUniversity(university);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setEmail(request.getEmail());
            userRepository.save(user);

            List<Permission> allPermissions = permissionRepository.findAll();
            for (Permission permission : allPermissions) {
                UserPermission userPermission = new UserPermission();
                userPermission.setUser(user);
                userPermission.setPermission(permission);
                userPermissionRepository.save(userPermission);
            }

            //gửi gmail
//        emailService.sendActivationEmail(nguoiDat.getEmail(),activationCode);
            return ResponseEntity.ok("Đăng ký thành công! Vui lòng kiểm tra email để kích hoạt tài khoản");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Xác thực tài khoản
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername();

            // Kiểm tra user trong database
            Optional<User> optionalUser =userRepository.findByEmail(email);
            User user = optionalUser.get();
            Optional<University> university= universityRepository.findByEmail(user.getEmail());

            // Lấy danh sách quyền của user
            String role = user.getRole().getName();

            List<UserPermission> userPermissions = userPermissionRepository.findByUserId(user.getId());
            List<String> authorities = userPermissions.stream()
                    .map(up -> up.getPermission().getName())
                    .collect(Collectors.toList());

            if( authorities == null || authorities.isEmpty()){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tài khoản đã bị khóa");
            }

            String token = jwtUtil.generateToken(user.getEmail(), List.of(role), authorities);// Tạo JWT token từ email và roles

            // Xác định URL chuyển hướng dựa trên vai trò
            String redirectUrl;
            switch (role) {
                case "PDT":
                    redirectUrl = "/dashboard/pdt";
                    break;
                case "ADMIN":
                    redirectUrl = "/dashboard/admin";
                    break;
                default:
                    redirectUrl = "/dashboard/default";
                    break;
            }

            // Trả về phản hồi với header Location để chuyển hướng
            return ResponseEntity.ok(new LoginResponse(
                    university.get().getName(),
                    user.getEmail(),
                    token,
                    role,
                    redirectUrl,
                    authorities
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email hoặc mật khẩu không đúng!");
        }
    }


//    @PostMapping("/verify")
//    public ResponseEntity<String> verifyActivationCode(@RequestBody VerifyRequest request) {
//        boolean isVerified = emailService.verifyActivationCode(request.getEmail(), request.getCode());
//        if (isVerified) {
//            return ResponseEntity.ok("Tài khoản của bạn đã được kích hoạt!");
//        } else {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mã kích hoạt không hợp lệ hoặc đã hết hạn!");
//        }
//    }
}
