package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);

    List<User> findByUniversity(University university);
    List<Department> findByDepartment(University university);
    boolean existsByEmail(String email);
    boolean existsById(Long id);

    Optional<User> findById(Long id);

    Optional<User> findByDepartment(Department department);

    @Query(value = """
            SELECT u.*
            FROM departments d
            join universitys un on d.university_id = un.id
            JOIN users u ON u.department_id = d.id
            JOIN user_permissions up ON up.user_id = u.id
            JOIN permissions p ON p.id = up.permission_id
            WHERE un.id = :universityId
            AND d.status = 'ACTIVE'
            AND (:nameDepartment IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :nameDepartment, '%')))
            GROUP BY d.id, d.name;        
    """, nativeQuery = true)
    List<User> findUserDepartmentByUniversity(@Param("universityId") Long universityId,
                                          @Param("nameDepartment") String nameDepartment);
}
