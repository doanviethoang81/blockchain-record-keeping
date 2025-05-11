package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.User;
import com.example.blockchain.record.keeping.models.UserPermission;

import java.util.List;

public interface IUserPermissionService {
    UserPermission save(UserPermission userPermission);
    List<UserPermission> listUserPermission(Long id);
    List<UserPermission> listUserPermissions(User user);
}
