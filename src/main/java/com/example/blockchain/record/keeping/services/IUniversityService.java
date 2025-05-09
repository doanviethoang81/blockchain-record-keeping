package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.University;


public interface IUniversityService {
    University getUniversityByEmail(String email);
}
