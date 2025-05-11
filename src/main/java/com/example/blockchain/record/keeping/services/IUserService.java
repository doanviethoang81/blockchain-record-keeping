package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.User;

import java.util.Optional;

public interface IUserService {
    User findByUser(String email);
    User save(User user);
}
