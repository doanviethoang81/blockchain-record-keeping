package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.University;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department,Long> {
    Optional<Department> findByName(String name);

    boolean existsByNameIgnoreCaseAndUniversityId(String name, Long universityId);
    List<Department> findByUniversity(University university);

}
