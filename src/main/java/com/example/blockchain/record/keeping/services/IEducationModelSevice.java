package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.request.EducationModeRequest;
import com.example.blockchain.record.keeping.dtos.request.RatingRequest;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.EducationMode;

import java.util.List;


public interface IEducationModelSevice {
    EducationMode findByName(String name);
    EducationMode findById(Long id);

    List<EducationMode> listEducationMode();

    EducationMode update(EducationMode educationMode, String name);

    EducationMode delete(EducationMode educationMode);

    boolean findByNameAndStatus(String name, Status status);

    EducationMode add(EducationModeRequest request);
}
