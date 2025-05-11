package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.Role;

public interface IRoleService {
    Role findByName(String name);
}
