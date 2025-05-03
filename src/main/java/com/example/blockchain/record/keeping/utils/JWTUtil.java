package com.example.blockchain.record.keeping.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Component
public class JWTUtil {

    private final Key SECRET_KEY = Keys.hmacShaKeyFor("6ce90e09a30b372bbaae83da5f825d3751978d7111fe88aea06452ca1dd5ec73".getBytes());

    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 10; // 10 tiếng
    // Tạo token
    public String generateToken(String email, List<String> roles) {
        return Jwts.builder()
                .setSubject(email)
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // 1 ngày
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    //Trích xuất email (subject) từ token
    public String getEmailFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    //Trích xuất roles hoặc authorities từ token (nếu có)
    @SuppressWarnings("unchecked")
    public List<String> getAuthoritiesFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return (List<String>) claims.get("authorities");
    }

    //Kiểm tra token có hết hạn không
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    //Trích xuất thời gian hết hạn
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    //Hàm hỗ trợ
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            System.out.println("Token hết hạn");
            throw e;
        } catch (SignatureException e) {
            System.out.println("Sai chữ ký token");
            throw e;
        } catch (MalformedJwtException e) {
            System.out.println("Token sai định dạng");
            throw e;
        } catch (Exception e) {
            System.out.println("Lỗi token: " + e.getMessage());
            throw e;
        }
    }

}
