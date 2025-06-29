package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StudentAuthenticationProvider implements AuthenticationProvider {
    @Autowired
    private StudentService studentService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();

        Student student = studentService.findByEmail(email);
        if (student == null) {
            throw new BadCredentialsException("Email không tồn tại");
        }

        if (!passwordEncoder.matches(password, student.getPassword())) {
            throw new BadCredentialsException("Sai mật khẩu");
        }

        return new UsernamePasswordAuthenticationToken(email, password, List.of(new SimpleGrantedAuthority("STUDENT")));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
