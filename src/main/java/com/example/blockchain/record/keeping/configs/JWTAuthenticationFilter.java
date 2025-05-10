package com.example.blockchain.record.keeping.configs;

import com.example.blockchain.record.keeping.response.ApiResponseBuilder;
import com.example.blockchain.record.keeping.services.CustomUserDetailService;
import com.example.blockchain.record.keeping.utils.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final CustomUserDetailService customUserDetailService;

    public JWTAuthenticationFilter(JWTUtil jwtUtil, CustomUserDetailService customUserDetailService) {
        this.jwtUtil = jwtUtil;
        this.customUserDetailService = customUserDetailService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try{
            String path = request.getRequestURI();
            //Bỏ qua các URL không cần kiểm tra token
            if (path.startsWith("/api/auth/")) {
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
            //Lấy token từ header: "Authorization: Bearer <token>"
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7); // Bỏ chữ "Bearer "
                email = jwtUtil.getEmailFromToken(jwt);
            }

            //Nếu có email và chưa đăng nhập (context chưa có user)
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = customUserDetailService.loadUserByUsername(email);

                if (!jwtUtil.isTokenExpired(jwt)) {
                    List<GrantedAuthority> authorities = jwtUtil.getAuthoritiesFromToken(jwt);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    authorities //phân quyền từ token
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
                else {
                    throw new RuntimeException("Token đã hết hạn");
                }
            }

            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            String errorJson = new ObjectMapper().writeValueAsString(
                    ApiResponseBuilder.unauthorized(ex.getMessage()).getBody()
            );

            response.getWriter().write(errorJson);
            response.getWriter().flush();
//            String errorJson = String.format("{\"error\": \"%s\"}", ex.getMessage());
//            response.getWriter().write(errorJson);
//            response.getWriter().flush();
        }
    }
}