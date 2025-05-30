package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.Student;
import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department,Long> {
    Optional<Department> findByName(String name);

    boolean existsByNameIgnoreCaseAndUniversityIdAndStatus(String name, Long universityId, Status status);

    List<Department> findByUniversity(University university);


    // tìm tên khoa trong 1 tr
    @Query(value = """
        SELECT d.* FROM departments d
        JOIN universitys u ON d.university_id = u.id
        WHERE d.name COLLATE utf8mb4_unicode_ci = :departmentName COLLATE utf8mb4_unicode_ci
        AND u.id = :universityId
        AND d.status = 'ACTIVE'
        ORDER BY d.updated_at DESC
        """, nativeQuery = true)
    Optional<Department> findByDepartmentNameOfUniversity(@Param("universityId") Long universityId,
                                                          @Param("departmentName") String departmentName);

}
