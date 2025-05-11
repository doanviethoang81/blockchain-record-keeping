package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.Permission;
import com.example.blockchain.record.keeping.models.University;

import java.util.List;

public interface IPermissionService {

    List<Permission> listPermission();

}
