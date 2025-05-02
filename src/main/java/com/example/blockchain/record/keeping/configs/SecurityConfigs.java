//package com.example.blockchain.record.keeping.configs;
//
//
//import com.example.blockchain.record.keeping.models.University;
//import com.example.blockchain.record.keeping.repositorys.RoleRepository;
//import com.example.blockchain.record.keeping.repositorys.UniversityRepository;
//import lombok.Getter;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
//import org.springframework.web.cors.CorsConfigurationSource;
//
//import java.net.URLEncoder;
//import java.util.Optional;
//import java.util.Set;
//
//@Getter
//@Configuration
//@EnableWebSecurity
//public class SecurityConfigs {
//
//
//    @Autowired
//    private RoleRepository roleRepostiory;
//
//    @Autowired
//    private UniversityRepository universityRepository;
//
//    @Autowired
//    private CorsConfigurationSource corsConfigurationSource;
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests((requests) -> requests
//                        .requestMatchers("/api/auth/**").permitAll()
//                        .requestMatchers("/api/v1/**").permitAll()
//                        .requestMatchers("/api/v1/posts/**").permitAll()
//                        .requestMatchers("/api/v1/payment/vn-pay-callback").permitAll()
//                        .requestMatchers("/oauth2/**").permitAll()
//                        .requestMatchers("/images/**").permitAll()
//                        .requestMatchers("/api/v1/admin/**").hasAuthority("ROLE_ADMIN") // Giới hạn quyền admin
//                        .requestMatchers("/admin/**").hasAuthority("admin") // ADMIN mới được vào /admin/**
//                        .anyRequest().authenticated()
//                )
//
//                .cors(cors -> cors.configurationSource(corsConfigurationSource))
//                .csrf(AbstractHttpConfigurer::disable);
//        return http.build();
//    }
//
////    private AuthenticationSuccessHandler authenticationSuccessHandler() {
////        return (request, response, authentication) -> {
////            Optional<University> university = universityRepository.findByEmail(email);
////
////            if (existingUser.isPresent()) {
////                NguoiDat nguoiDat = existingUser.get();
////                if ("LOCAL".equals(nguoiDat.getProvider())) {
////                    String redirectUrlError = "https://chude2-nhom14.netlify.app?error=" +
////                            URLEncoder.encode("Tài khoản này đã đăng ký bằng email vui lòng đăng nhập bằng mật khẩu!", "UTF-8");
////                    response.setHeader("Content-Type", "text/html; charset=UTF-8");
////                    response.sendRedirect(redirectUrlError);
////                    return;
////                }
////                nguoiDat.setTen(firstName);
////                nguoiDatRepository.save(nguoiDat);
////            } else {
////                NguoiDat nguoiDat = new NguoiDat();
////                nguoiDat.setEmail(email);
////                nguoiDat.setTen(firstName);
////                nguoiDat.setEnable(true);
////                nguoiDat.setProvider("GOOGLE");
////                Role userRole = roleRepostiory.findByName("ROLE_USER")
////                        .orElseGet(() -> {
////                            Role newRole = new Role();
////                            newRole.setName("ROLE_USER");
////                            return roleRepostiory.save(newRole);
////                        });
////                nguoiDat.setRoles(Set.of(userRole));
////                nguoiDatRepository.save(nguoiDat);
////            }
////
////            String tokenJWT = jwtUtil.generateToken(email, Set.of("ROLE_USER"));
////            String redirectUrl = "https://chude2-nhom14.netlify.app?name=" + URLEncoder.encode(firstName, "UTF-8") +
////                    "&avatar=" + avatar +
////                    "&email=" + email +
////                    "&token=" + tokenJWT;
////            response.setHeader("Content-Type", "text/html; charset=UTF-8");
////            response.sendRedirect(redirectUrl);
////        };
////    }
//}