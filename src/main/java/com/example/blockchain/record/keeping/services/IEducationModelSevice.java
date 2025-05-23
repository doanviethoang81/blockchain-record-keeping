package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.EducationMode;


public interface IEducationModelSevice {
    EducationMode findByName(String name);
}
