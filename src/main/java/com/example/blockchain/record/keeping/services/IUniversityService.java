package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.request.UniversityRequest;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.models.User;
import com.example.blockchain.record.keeping.response.ApiResponse;
import com.example.blockchain.record.keeping.response.ApiResponseBuilder;

import java.io.IOException;
import java.util.List;


public interface IUniversityService {
    University getUniversityByEmail(String email);

    List<User> findAllUserUniversity(String nameUniversity);

    University findById(Long id);

    void update(University university, UniversityRequest universityRequest) throws IOException;

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    University save(University university);
}
