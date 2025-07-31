package com.example.blockchain.record.keeping.configs;

import com.example.blockchain.record.keeping.models.Student;
import com.example.blockchain.record.keeping.response.ApiResponseBuilder;
import com.example.blockchain.record.keeping.services.CustomUserDetailService;
import com.example.blockchain.record.keeping.services.StudentService;
import com.example.blockchain.record.keeping.services.TokenBlacklistService;
import com.example.blockchain.record.keeping.utils.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final CustomUserDetailService customUserDetailService;
    private final TokenBlacklistService tokenBlacklistService;
    private final StudentService studentService;
    public JWTAuthenticationFilter(JWTUtil jwtUtil, CustomUserDetailService customUserDetailService, TokenBlacklistService tokenBlacklistService, StudentService studentService) {
        this.jwtUtil = jwtUtil;
        this.customUserDetailService = customUserDetailService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.studentService = studentService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        // Bỏ qua yêu cầu OPTIONS
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            filterChain.doFilter(request, response);
            return;
        }
        try{
            String path = request.getRequestURI();
            //bỏ qua các URL không cần kiểm tra token
            if (path.startsWith("/api/auth/")
                    || path.startsWith("/api/v1/verify")
                    || path.startsWith("/api/v1/verify/")
                    || path.startsWith("/v3/api-docs")
                    || path.startsWith("/swagger-ui")
                    || path.startsWith("/ws")
                    || path.equals("/swagger-ui.html")) {
                filterChain.doFilter(request, response);
                return;
            }

            final String authHeader = request.getHeader("Authorization");

            // Kiểm tra và lấy token từ header Authorization
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("Thiếu token hoặc không đúng định dạng Bearer");
            }

            String email = null;
            String jwt = null;
            List<String> roles = new ArrayList<>();
            //Lấy token từ header: "Authorization: Bearer <token>"
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7); // Bỏ chữ "Bearer "

                if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
                    throw new RuntimeException("Tài khoản đã đăng xuất");
                }
                email = jwtUtil.getEmailFromToken(jwt);
                roles = jwtUtil.getRolesFromToken(jwt);
            }

            //Nếu có email và chưa đăng nhập (context chưa có user)
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = null;

                if (roles.contains("STUDENT")) {
                    Student student = studentService.findByEmail(email);
                    if (student == null) {
                        throw new RuntimeException("Không tìm thấy sinh viên!");
                    }

                    //tạo UserDetails thủ công cho sinh viên
                    userDetails = new org.springframework.security.core.userdetails.User(
                            student.getEmail(),
                            student.getPassword(),
                            List.of(new SimpleGrantedAuthority("STUDENT"))
                    );

                } else {
                    userDetails = customUserDetailService.loadUserByUsername(email);
                }

                if (!jwtUtil.isTokenExpired(jwt)) {
                    List<GrantedAuthority> authorities = jwtUtil.getAuthoritiesFromToken(jwt);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    authorities
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                } else {
                    throw new RuntimeException("Token đã hết hạn");
                }
            }


            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            if (!response.isCommitted()) {
                response.reset();

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                String errorJson = new ObjectMapper().writeValueAsString(
                        ApiResponseBuilder.unauthorized(ex.getMessage()).getBody()
                );

                response.getWriter().write(errorJson);
                response.getWriter().flush();
            }
            return;
        }
    }
}