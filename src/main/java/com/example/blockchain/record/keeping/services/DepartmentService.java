package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.repositorys.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService  implements IDepartmentService{

    private final DepartmentRepository departmentRepository;

    @Override
    public Department save(Department department) {
        return departmentRepository.save(department);
    }

    @Override
    public boolean existsByNameAndUniversity(String name, Long universityId) {
        return departmentRepository.existsByNameIgnoreCaseAndUniversityId(name.trim(), universityId);
    }

    @Override
    public List<Department> listDepartmentOfUniversity(University university) {
        return departmentRepository.findByUniversity(university);
    }


}
