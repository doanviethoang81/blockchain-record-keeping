package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.User;
import com.example.blockchain.record.keeping.models.UserPermission;
import com.example.blockchain.record.keeping.repositorys.UserPermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPermissionService implements IUserPermissionService{

    private final UserPermissionRepository userPermissionRepository;
    @Override
    public UserPermission save(UserPermission userPermission) {
        return userPermissionRepository.save(userPermission);
    }

    @Override
    public List<UserPermission> listUserPermission(Long id) {
        return userPermissionRepository.findByUserId(id);
    }

    @Override
    public List<UserPermission> listUserPermissions(User user) {
        return userPermissionRepository.findByUser(user);
    }
}
