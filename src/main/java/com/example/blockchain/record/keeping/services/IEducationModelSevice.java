package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.EducationMode;

import java.util.List;


public interface IEducationModelSevice {
    EducationMode findByName(String name);
    EducationMode findById(Long id);

    List<EducationMode> listEducationMode();
}
