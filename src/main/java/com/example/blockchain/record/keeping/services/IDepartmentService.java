package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.University;

import java.util.List;
import java.util.Optional;

public interface IDepartmentService {
    Department save(Department department);

    boolean existsByNameAndUniversity(String name, Long universityId);
    boolean existsByNameIgnoreCaseAndUniversityIdAndStatusAndIdNot( String name, Long universityId, Long departmentId);

    List<Department> listDepartmentOfUniversity(University university);

    Department updateDepartment(Long id, String name,String email);

    Department deleteDepartment(Long id);

    Department findById(Long id);

    Optional<Department> findByDepartmentNameOfUniversity(Long universityId, String name);
}
