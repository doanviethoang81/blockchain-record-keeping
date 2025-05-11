package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.Department;

public interface IDepartmentService {
    Department save(Department department);

    boolean existsByNameAndUniversity(String name, Long universityId);
}
