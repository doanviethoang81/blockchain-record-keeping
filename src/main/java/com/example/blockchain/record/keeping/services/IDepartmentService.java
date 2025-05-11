package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.University;

import java.util.List;

public interface IDepartmentService {
    Department save(Department department);

    boolean existsByNameAndUniversity(String name, Long universityId);

    List<Department> listDepartmentOfUniversity(University university);

}
