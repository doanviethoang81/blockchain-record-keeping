package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.Role;
import com.example.blockchain.record.keeping.repositorys.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoleService implements IRoleService{
    private final RoleRepository roleRepository;
    @Override
    public Role findByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy role"+ name));
    }
}
