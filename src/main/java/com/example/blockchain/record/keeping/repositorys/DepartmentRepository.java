package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.Student;
import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface DepartmentRepository extends JpaRepository<Department,Long> {
    Optional<Department> findByName(String name);

    boolean existsByNameIgnoreCaseAndUniversityIdAndStatus(String name, Long universityId, Status status);

    // kiểm tra bỏ id đó
    boolean existsByNameIgnoreCaseAndUniversityIdAndStatusAndIdNot(String name,Long universityId,Status status,Long id);

    //danh sách khoa của 1 trường
    List<Department> findByUniversityAndStatus(University university, Status status);

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

    //kiem tra xem có lớp thuộc khoa không de xoa khoa
    @Query(value = """
        SELECT COUNT(*) AS count_class
        FROM student_class sc
        WHERE sc.department_id = :id
        and sc.status = 'ACTIVE'
        """,nativeQuery = true)
    long countClassOfDepartment(@Param("id") Long id);


    @Query("SELECT d FROM Department d WHERE d.university.id = :universityId AND d.name IN :names AND d.status = 'ACTIVE'")
    List<Department> findByUniversityIdAndNames(@Param("universityId") Long universityId, @Param("names") Set<String> names);

}
