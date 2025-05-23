package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.StudentClass;

import java.util.List;
import java.util.Optional;

public interface IStudentClassService {
    StudentClass findById(Long id);
    StudentClass findByName(String name);

    List<StudentClass>  findAllClassesByDepartmentId(Long id);
}
