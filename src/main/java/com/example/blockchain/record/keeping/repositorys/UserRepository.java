package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.dtos.StatisticsAdminDTO;
import com.example.blockchain.record.keeping.dtos.StatisticsDepartmentDTO;
import com.example.blockchain.record.keeping.dtos.StatisticsUniversityDTO;
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

    User findByUniversity(University university);
    List<Department> findByDepartment(University university);
    boolean existsByEmail(String email);
    boolean existsById(Long id);

    Optional<User> findById(Long id);

    Optional<User> findByDepartment(Department department);

    // danh sach khoa thuoc 1 truong va tim ten
    @Query(value = """
            SELECT u.*
            FROM departments d
            join universitys un on d.university_id = un.id
            JOIN users u ON u.department_id = d.id
            JOIN user_permissions up ON up.user_id = u.id
            JOIN permissions p ON p.id = up.permission_id
            WHERE un.id = :universityId
            AND u.department_id IS NOT NULL
            AND d.status = 'ACTIVE'
            AND (:nameDepartment IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :nameDepartment, '%')))
            ORDER BY u.updated_at DESC       
    """, nativeQuery = true)
    List<User> findUserDepartmentByUniversity(@Param("universityId") Long universityId,
                                          @Param("nameDepartment") String nameDepartment);

    // thống kê admin
    @Query(value = """
    SELECT
        (SELECT COUNT(*) FROM universitys un
         JOIN users u ON un.id = u.university_id) AS university_count,
        (SELECT COUNT(*) FROM students WHERE students.status = 'ACTIVE') AS student_count,
        (SELECT COUNT(*) FROM certificates WHERE certificates.status = 'PENDING') AS certificate_Pending,
        (SELECT COUNT(*) FROM certificates WHERE certificates.status = 'APPROVED') AS certificate_Approved,
        (SELECT COUNT(*) FROM certificates WHERE certificates.status = 'REJECTED') AS certificate_Rejected,
        (SELECT COUNT(*) FROM degrees WHERE degrees.status = 'PENDING') AS degrees_Pending,
        (SELECT COUNT(*) FROM degrees WHERE degrees.status = 'APPROVED') AS degrees_Approved,
        (SELECT COUNT(*) FROM degrees WHERE degrees.status = 'REJECTED') AS degrees_Rejected
    """, nativeQuery = true)
    StatisticsAdminDTO getStatisticsAdmin();

    @Query(value = """
    SELECT
        -- 1. Số khoa (departments) active
        (SELECT COUNT(*) 
         FROM departments d 
         JOIN users u ON u.department_id = d.id 
         WHERE u.university_id = :universityId AND d.status = 'ACTIVE') AS departmentCount,

        -- 2. Số lớp thuộc khoa active
        (SELECT COUNT(*) 
         FROM student_class sc 
         JOIN departments d ON sc.department_id = d.id 
         JOIN users u ON u.department_id = d.id 
         WHERE u.university_id = :universityId AND d.status = 'ACTIVE') AS classCount,

        -- 3. Số sinh viên thuộc lớp và khoa active, sinh viên active
        (SELECT COUNT(*) 
         FROM students s 
         JOIN student_class sc ON s.student_class_id = sc.id 
         JOIN departments d ON sc.department_id = d.id 
         JOIN users u ON u.department_id = d.id 
         WHERE u.university_id = :universityId AND d.status = 'ACTIVE' AND s.status = 'ACTIVE') AS studentCount,

        -- 4-6. Chứng chỉ theo trạng thái, thuộc sinh viên active, lớp và khoa active
        (SELECT COUNT(*) 
         FROM certificates c 
         JOIN students s ON c.student_id = s.id 
         JOIN student_class sc ON s.student_class_id = sc.id 
         JOIN departments d ON sc.department_id = d.id 
         JOIN users u ON u.department_id = d.id 
         WHERE u.university_id = :universityId AND d.status = 'ACTIVE' AND s.status = 'ACTIVE' AND c.status = 'PENDING') AS certificatePending,

        (SELECT COUNT(*) 
         FROM certificates c 
         JOIN students s ON c.student_id = s.id 
         JOIN student_class sc ON s.student_class_id = sc.id 
         JOIN departments d ON sc.department_id = d.id 
         JOIN users u ON u.department_id = d.id 
         WHERE u.university_id = :universityId AND d.status = 'ACTIVE' AND s.status = 'ACTIVE' AND c.status = 'APPROVED') AS certificateApproved,

        (SELECT COUNT(*) 
         FROM certificates c 
         JOIN students s ON c.student_id = s.id 
         JOIN student_class sc ON s.student_class_id = sc.id 
         JOIN departments d ON sc.department_id = d.id 
         JOIN users u ON u.department_id = d.id 
         WHERE u.university_id = :universityId AND d.status = 'ACTIVE' AND s.status = 'ACTIVE' AND c.status = 'REJECTED') AS certificateRejected,

        -- 7-9. Văn bằng theo trạng thái, thuộc sinh viên active, lớp và khoa active
        (SELECT COUNT(*) 
         FROM degrees dgr 
         JOIN students s ON dgr.student_id = s.id 
         JOIN student_class sc ON s.student_class_id = sc.id 
         JOIN departments d ON sc.department_id = d.id 
         JOIN users u ON u.department_id = d.id 
         WHERE u.university_id = :universityId AND d.status = 'ACTIVE' AND s.status = 'ACTIVE' AND dgr.status = 'PENDING') AS degreePending,

        (SELECT COUNT(*) 
         FROM degrees dgr 
         JOIN students s ON dgr.student_id = s.id 
         JOIN student_class sc ON s.student_class_id = sc.id 
         JOIN departments d ON sc.department_id = d.id 
         JOIN users u ON u.department_id = d.id 
         WHERE u.university_id = :universityId AND d.status = 'ACTIVE' AND s.status = 'ACTIVE' AND dgr.status = 'APPROVED') AS degreeApproved,

        (SELECT COUNT(*) 
         FROM degrees dgr 
         JOIN students s ON dgr.student_id = s.id 
         JOIN student_class sc ON s.student_class_id = sc.id 
         JOIN departments d ON sc.department_id = d.id 
         JOIN users u ON u.department_id = d.id 
         WHERE u.university_id = :universityId AND d.status = 'ACTIVE' AND s.status = 'ACTIVE' AND dgr.status = 'REJECTED') AS degreeRejected
    """, nativeQuery = true)
    StatisticsUniversityDTO getStatisticsUniversity(@Param("universityId") Long universityId);


    @Query(value = """
            SELECT
                -- Số lượng lớp hoạt động trong khoa
                (SELECT COUNT(*) 
                 FROM student_class sc 
                 WHERE sc.department_id = :departmentId AND sc.status = 'ACTIVE') AS class_count,
            
                -- Số lượng sinh viên hoạt động thuộc lớp hoạt động trong khoa
                (SELECT COUNT(*) 
                 FROM students s 
                 JOIN student_class sc ON s.student_class_id = sc.id 
                 WHERE sc.department_id = :departmentId 
                   AND sc.status = 'ACTIVE' 
                   AND s.status = 'ACTIVE') AS student_count,
            
                -- Chứng chỉ theo trạng thái, lớp hoạt động, sinh viên hoạt động
                (SELECT COUNT(*) 
                 FROM certificates c 
                 JOIN students s ON c.student_id = s.id 
                 JOIN student_class sc ON s.student_class_id = sc.id 
                 WHERE sc.department_id = :departmentId 
                   AND sc.status = 'ACTIVE' 
                   AND s.status = 'ACTIVE' 
                   AND c.status = 'PENDING') AS certificate_pending,
            
                (SELECT COUNT(*) 
                 FROM certificates c 
                 JOIN students s ON c.student_id = s.id 
                 JOIN student_class sc ON s.student_class_id = sc.id 
                 WHERE sc.department_id = :departmentId 
                   AND sc.status = 'ACTIVE' 
                   AND s.status = 'ACTIVE' 
                   AND c.status = 'APPROVED') AS certificate_approved,
            
                (SELECT COUNT(*) 
                 FROM certificates c 
                 JOIN students s ON c.student_id = s.id 
                 JOIN student_class sc ON s.student_class_id = sc.id 
                 WHERE sc.department_id = :departmentId 
                   AND sc.status = 'ACTIVE' 
                   AND s.status = 'ACTIVE' 
                   AND c.status = 'REJECTED') AS certificate_rejected,
            
                -- Văn bằng theo trạng thái, lớp hoạt động, sinh viên hoạt động
                (SELECT COUNT(*) 
                 FROM degrees d 
                 JOIN students s ON d.student_id = s.id 
                 JOIN student_class sc ON s.student_class_id = sc.id 
                 WHERE sc.department_id = :departmentId 
                   AND sc.status = 'ACTIVE' 
                   AND s.status = 'ACTIVE' 
                   AND d.status = 'PENDING') AS degree_pending,
            
                (SELECT COUNT(*) 
                 FROM degrees d 
                 JOIN students s ON d.student_id = s.id 
                 JOIN student_class sc ON s.student_class_id = sc.id 
                 WHERE sc.department_id = :departmentId 
                   AND sc.status = 'ACTIVE' 
                   AND s.status = 'ACTIVE' 
                   AND d.status = 'APPROVED') AS degree_approved,
            
                (SELECT COUNT(*) 
                 FROM degrees d 
                 JOIN students s ON d.student_id = s.id 
                 JOIN student_class sc ON s.student_class_id = sc.id 
                 WHERE sc.department_id = :departmentId 
                   AND sc.status = 'ACTIVE' 
                   AND s.status = 'ACTIVE' 
                   AND d.status = 'REJECTED') AS degree_rejected
            
            """, nativeQuery = true)
    StatisticsDepartmentDTO getStatisticsDepartment(@Param("departmentId") Long departmentId);

}
