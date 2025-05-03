package com.example.blockchain.record.keeping.configs;


import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.repositorys.RoleRepository;
import com.example.blockchain.record.keeping.repositorys.UniversityRepository;
import com.example.blockchain.record.keeping.services.CustomUserDetailService;
import com.example.blockchain.record.keeping.utils.JWTUtil;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
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

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    public SecurityConfigs(CustomUserDetailService customUserDetailService, JWTUtil jwtUtil) {
        this.customUserDetailService = customUserDetailService;
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .addFilterBefore( jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/api/auth/**").permitAll()
//                        .requestMatchers("/api/v1/**").permitAll()
//                        .requestMatchers("/api/v1/posts/**").permitAll()
//                        .requestMatchers("/images/**").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasAuthority("ADMIN") // Giới hạn quyền admin
                        .anyRequest().authenticated()
                )

                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

}