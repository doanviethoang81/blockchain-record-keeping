package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.dtos.StatisticsAdminDTO;
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
            GROUP BY d.id, d.name
            ORDER BY u.updated_at DESC       
    """, nativeQuery = true)
    List<User> findUserDepartmentByUniversity(@Param("universityId") Long universityId,
                                          @Param("nameDepartment") String nameDepartment);

    // thống kê admin
    @Query(value = """
    SELECT
        (SELECT COUNT(*) FROM universitys un
         join users u on un.id = u.university_id) AS university_count,
        (SELECT COUNT(*) FROM students WHERE students.status ='ACTIVE' ) AS student_count,
        (SELECT COUNT(*) FROM certificates WHERE certificates.status ='PENDING') AS certificate_Pending,
        (SELECT COUNT(*) FROM certificates WHERE certificates.status ='APPROVED') AS certificate_Approved,
        (SELECT COUNT(*) FROM certificates WHERE certificates.status ='REJECTED') AS certificate_Rejected,
        (SELECT COUNT(*) FROM degrees WHERE degrees.status ='PENDING') AS degrees_Pending,
        (SELECT COUNT(*) FROM degrees WHERE degrees.status ='APPROVED') AS degrees_Approved,
        (SELECT COUNT(*) FROM degrees WHERE degrees.status ='REJECTED') AS degrees_Rejected;
    """, nativeQuery = true)
    StatisticsAdminDTO getStatisticsAdmin();

//    // thống kê university
//    @Query(value = """
//    SELECT
//        (SELECT COUNT(*) FROM universitys un
//         join users u on un.id = u.university_id) AS university_count,
//        (SELECT COUNT(*) FROM students WHERE students.status ='ACTIVE' ) AS student_count,
//        (SELECT COUNT(*) FROM certificates WHERE certificates.status ='PENDING') AS certificate_Pending,
//        (SELECT COUNT(*) FROM certificates WHERE certificates.status ='APPROVED') AS certificate_Approved,
//        (SELECT COUNT(*) FROM certificates WHERE certificates.status ='REJECTED') AS certificate_Rejected,
//        (SELECT COUNT(*) FROM degrees WHERE degrees.status ='PENDING') AS degrees_Pending,
//        (SELECT COUNT(*) FROM degrees WHERE degrees.status ='APPROVED') AS degrees_Approved,
//        (SELECT COUNT(*) FROM degrees WHERE degrees.status ='REJECTED') AS degrees_Rejected;
//    """, nativeQuery = true)
//    StatisticsAdminDTO getStatisticsUniversity();


}
