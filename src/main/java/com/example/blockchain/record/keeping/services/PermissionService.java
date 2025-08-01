package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.Permission;
import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.repositorys.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionService implements IPermissionService{
    private final PermissionRepository permissionRepository;


    @Override
    public List<Permission> listPermission() {
        return permissionRepository.findAll();
    }

    public boolean hasPermission(Authentication auth, String permission) {
        String username = auth.getName();
        Integer result = permissionRepository.existsByUsernameAndPermission(username, permission);
        return result != null && result == 1;// nếu cả 2 đúng thì trả về true
    }

}
