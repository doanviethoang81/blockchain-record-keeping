package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.StudentClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentClassRepository extends JpaRepository<StudentClass,Long> {

    Optional<StudentClass> findByName(String name);

    //kiem tra lop co ton tai trong khoa nay k
    boolean existsByIdAndDepartmentId(Long classId, Long departmentId);

    // lay ds lop cua 1 tr theo id trường và tìm lớp
    @Query(value = """
        SELECT s.* 
        FROM student_class s
        JOIN departments d ON d.id = s.department_id
        JOIN universitys u ON d.university_id = u.id
        WHERE u.id = :universityId 
          AND s.status = 'ACTIVE'
          And d.status = 'ACTIVE'
          AND (:className IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :className, '%')))
          ORDER BY s.updated_at DESC
    """, nativeQuery = true)
    List<StudentClass> findAllClassOfUniversityByName(
            @Param("universityId") Long universityId,
            @Param("className") String className
    );


    //lay ds lop theo khoa vs tìm lớp (khoa)
    @Query(value = """
        SELECT sc.* FROM student_class sc
        JOIN departments d ON sc.department_id = d.id
        WHERE d.id = :departmentId
        and sc.status='ACTIVE'
        AND (:className IS NULL OR LOWER(sc.name) LIKE LOWER(CONCAT('%', :className, '%')))
        ORDER BY sc.created_at DESC
        """, nativeQuery = true)
    List<StudentClass> findAllClassesByDepartmentId(
            @Param("departmentId") Long departmentId,
            @Param("className") String className);

    //danh sách các khoa của 1 tr
    @Query(value = """
            select d.* from departments d
            JOIN universitys u on d.university_id =u.id
            where u.id = :universityId and d.status='ACTIVE'
            ORDER BY d.created_at DESC
            """, nativeQuery = true)
    List<Department> findAllDeparmentOfUniversity(@Param("universityId") Long universityId);

    //kiểm tra tên lớp tồn tại k
    boolean existsByNameAndDepartmentAndStatus(String name, Department department, Status status);

    // ds lớp và tìm lop theo ten
    List<StudentClass> findByNameContainingAndStatus(String keyword, Status status);

    //tìm tên lớp có thuộc khoa không (PDT)
    @Query(value = """
        SELECT sc.* FROM student_class sc
        JOIN departments d ON sc.department_id = d.id
        WHERE sc.name COLLATE utf8mb4_unicode_ci = :className COLLATE utf8mb4_unicode_ci
            AND sc.department_id = :departmentId 
            AND sc.status='ACTIVE'
            AND d.status = 'ACTIVE'
        """, nativeQuery = true)
    Optional<StudentClass> findByClassNameAndDepartmentId(
            @Param("departmentId") Long departmentId,
            @Param("className") String className);

    //kiem tra de xoa lop
    @Query(value = """
            SELECT COUNT(*) AS count_student
            FROM students s
            WHERE s.student_class_id = :id
            and s.status = 'ACTIVE'
            """,nativeQuery = true)
    long countStudentOfClass(@Param("id") Long id);

}
