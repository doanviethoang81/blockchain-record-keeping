package com.example.blockchain.record.keeping.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JWTUtil {

    String jwtSecret  = EnvUtil.get("JWT_SECRET_KEY");
    private final Key SECRET_KEY = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
//    private final Key SECRET_KEY = Keys.hmacShaKeyFor("6ce90e09a30b372bbaae83da5f825d3751978d7111fe88aea06452ca1dd5ec73".getBytes());

    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 10; // 10 tiếng
    // Tạo token
    public String generateToken(String email, List<String> roles, List<String> permissions) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        claims.put("permissions", permissions);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 1 ngày
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            throw new RuntimeException("Token đã hết hạn! ");
        }
    }

    public String getEmailFromToken(String token) {
        return getAllClaimsFromToken(token).getSubject();
    }

    public List<String> getRolesFromToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Object rolesObject = claims.get("roles");

            if (rolesObject instanceof List<?>) {
                return ((List<?>) rolesObject).stream()
                        .map(Object::toString)
                        .collect(Collectors.toList());
            }

            return List.of(); // Không có roles
        } catch (Exception e) {
            return List.of(); // Token lỗi hoặc không có claim "roles"
        }
    }

    public boolean isTokenExpired(String token) {
        Date expiration = getAllClaimsFromToken(token).getExpiration();
        return expiration.before(new Date());
    }

    //Phương thức thêm mới để lấy GrantedAuthority
    public List<GrantedAuthority> getAuthoritiesFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);

        List<String> roles = claims.get("roles", List.class);
        List<String> permissions = claims.get("permissions", List.class);

        List<GrantedAuthority> authorities = new ArrayList<>();

        if (roles != null) {
            authorities.addAll(roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .toList());
        }

        if (permissions != null) {
            authorities.addAll(permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList());
        }

        return authorities;
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
