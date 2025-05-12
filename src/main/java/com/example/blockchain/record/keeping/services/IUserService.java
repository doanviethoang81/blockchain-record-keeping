package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.models.User;

import java.util.List;
import java.util.Optional;

public interface IUserService {
    User findByUser(String email);
    User save(User user);
    List<User> listUser(University university);
    boolean isEmailRegistered(String email);

}
