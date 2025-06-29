package com.example.blockchain.record.keeping.configs;


import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.repositorys.RoleRepository;
import com.example.blockchain.record.keeping.repositorys.UniversityRepository;
import com.example.blockchain.record.keeping.response.ApiResponse;
import com.example.blockchain.record.keeping.response.ApiResponseBuilder;
import com.example.blockchain.record.keeping.services.CustomUserDetailService;
import com.example.blockchain.record.keeping.services.StudentAuthenticationProvider;
import com.example.blockchain.record.keeping.utils.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
public class SecurityConfigs {

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    private final CustomUserDetailService customUserDetailService;
    private final JWTUtil jwtUtil;
    @Autowired
    private JWTAuthenticationFilter jwtTokenFilter;

    @Autowired
    private RoleRepository roleRepostiory;

    @Autowired
    private CustomUserDetailService customUserDetailsService;

    @Autowired
    private StudentAuthenticationProvider studentAuthenticationProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);

        authBuilder.authenticationProvider(daoAuthenticationProvider()); // dùng cho admin/pdt/khoa
        authBuilder.authenticationProvider(studentAuthenticationProvider); // dùng cho student

        return authBuilder.build();
    }

    public SecurityConfigs(CustomUserDetailService customUserDetailService, JWTUtil jwtUtil) {
        this.customUserDetailService = customUserDetailService;
        this.jwtUtil = jwtUtil;
    }


    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .exceptionHandling(exception -> exception
                        .accessDeniedHandler(customAccessDeniedHandler())
                        .authenticationEntryPoint(new Http403ForbiddenEntryPoint()) // nếu không đăng nhập
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api/v1/verify/**",
                                "/api/v1/verify"
                        ).permitAll()
                        .requestMatchers("/api/v1/check-role").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/pdt/**").hasRole("PDT")
                        .requestMatchers("/api/v1/khoa/**").hasRole("KHOA")
                        .requestMatchers("/api/v1/student/**").hasAnyRole("STUDENT")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            ApiResponse<?> apiResponse = ApiResponse.builder()
                    .status(HttpStatus.FORBIDDEN.value())
                    .message("Bạn không có quyền thực hiện hành động này!")
                    .data(null)
                    .build();

            String json = new ObjectMapper().writeValueAsString(apiResponse);

            response.getWriter().write(json);
            response.getWriter().flush();
        };
    }
}