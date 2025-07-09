package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.models.User;

import java.util.List;


public interface IUniversityService {
    University getUniversityByEmail(String email);

    List<User> findAllUserUniversity(String nameUniversity);

    University findById(Long id);

    University save(University university);

}
