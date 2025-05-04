package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.Role;
import com.example.blockchain.record.keeping.models.User;
import com.example.blockchain.record.keeping.models.UserPermission;
import com.example.blockchain.record.keeping.repositorys.RoleRepository;
import com.example.blockchain.record.keeping.repositorys.UserPermissionRepository;
import com.example.blockchain.record.keeping.repositorys.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final RoleRepository roleRepository;

//    @Autowired
//    public CustomUserDetailService(UserRepository userRepository1, UserPermissionRepository userPermissionRepository) {
//        this.userRepository = userRepository1;
//        this.userPermissionRepository = userPermissionRepository;
//    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User không tồn tại"));
        List<UserPermission> userPermissions = userPermissionRepository.findByUserId(user.getId());
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().getName()))
        );
//        Role role = user.getRole();
//        List<UserPermission> userPermissions = userPermissionRepository.findByUserId(user.getId());
//        List<String> permissions = userPermissions.stream()
//                .map(up -> up.getPermission().getName()) // "READ", "WRITE"
//                .collect(Collectors.toList());
//
//        return new CustomUserDetails(user,role, permissions);
    }
}
