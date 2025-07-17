package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.request.DepartmentRequest;
import com.example.blockchain.record.keeping.dtos.request.UserDepartmentRequest;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.University;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IDepartmentService {
    Department save(Department department);
    List<Department> saveAll(List<Department> department);

    boolean existsByNameAndUniversity(String name, Long universityId);
    boolean existsByNameIgnoreCaseAndUniversityIdAndStatusAndIdNot( String name, Long universityId, Long departmentId);

    List<Department> listDepartmentOfUniversity(University university);

    Department updateDepartment(Long id,  DepartmentRequest request);

    Department deleteDepartment(Long id);

    Department findById(Long id);

    Optional<Department> findByDepartmentNameOfUniversity(Long universityId, String name);

    Department create(UserDepartmentRequest request, University university);

    long countClassOfDepartment(Long id);

    List<Department> findByUniversityIdAndNames(Long universityId, Set<String> names);

}
