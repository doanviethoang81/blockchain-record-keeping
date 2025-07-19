package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.dtos.request.CountCertificateTypeRequest;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.Certificate;
import com.example.blockchain.record.keeping.models.Student;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate,Long> {
    List<Certificate> findByStudent(Student student);

    // kiem tra xem chung chi nay da cap cho sv nay chua
    @Query(value = """
            Select c.* from certificates c
            join university_certificate_types uc on c.university_certificate_type_id = uc.id
            JOIN certificate_types ct on uc.certificate_type_id= ct.id
            WHERE  c.student_id = :studentId
            and ct.id = :certificateTypeId
            and ct.status ='ACTIVE'
            AND c.status IN ('PENDING', 'APPROVED', 'DELETED')
            """,nativeQuery = true)
    Optional<Certificate> existingStudentOfCertificate(@Param("studentId") Long studentId,
                                                       @Param("certificateTypeId") Long certificateTypeId);

    Certificate findByIdAndStatus(Long id, Status status);

    //danh sách ch chỉ của 1 khoa
    @Query(value = """
            select c.* from certificates c
            join students s on c.student_id = s.id
            join student_class sc on s.student_class_id= sc.id
            join departments d on sc.department_id = d.id
            where d.id= :departmentId
            and s.status ='ACTIVE'
            and sc.status ='ACTIVE'      
            AND c.status NOT LIKE 'DELETED'            
            AND (:className IS NULL OR sc.name LIKE CONCAT('%', :className, '%'))
            AND (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
            AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%'))
            AND (:diplomaNumber IS NULL OR c.diploma_number LIKE CONCAT('%', :diplomaNumber, '%'))
            ORDER BY c.updated_at DESC   
            LIMIT :limit OFFSET :offset 
            """,nativeQuery = true)
    List<Certificate> listCertificateOfDepartment(@Param("departmentId") Long departmentId,
                                                  @Param("className") String className,
                                                  @Param("studentCode") String studentCode,
                                                  @Param("studentName") String studentName,
                                                  @Param("diplomaNumber") String diplomaNumber,
                                                  @Param("limit") int limit,
                                                  @Param("offset") int offset
    );

    //count số lượng ch ch all khoa
    @Query(value = """
        SELECT COUNT(*) FROM certificates c
        JOIN students s ON c.student_id = s.id
        JOIN student_class sc ON s.student_class_id = sc.id
        JOIN departments d ON sc.department_id = d.id
        WHERE d.id = :departmentId
        AND s.status = 'ACTIVE'
        AND sc.status = 'ACTIVE'
        AND d.status = 'ACTIVE'
        AND c.status NOT LIKE 'DELETED'
        AND (:className IS NULL OR sc.name LIKE CONCAT('%', :className, '%'))
        AND (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
        AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%'))
        AND (:diplomaNumber IS NULL OR c.diploma_number LIKE CONCAT('%', :diplomaNumber, '%'))
        ORDER BY c.updated_at DESC     
        """, nativeQuery = true)
    long countCertificatesOfDepartment(@Param("departmentId") Long departmentId,
                                       @Param("className") String className,
                                       @Param("studentCode") String studentCode,
                                       @Param("studentName") String studentName,
                                       @Param("diplomaNumber") String diplomaNumber);

    //count số lượng ch ch status khoa
    @Query(value = """
        SELECT COUNT(*) FROM certificates c
        JOIN students s ON c.student_id = s.id
        JOIN student_class sc ON s.student_class_id = sc.id
        JOIN departments d ON sc.department_id = d.id
        WHERE d.id = :departmentId
        AND s.status = 'ACTIVE'
        AND sc.status = 'ACTIVE'
        AND d.status = 'ACTIVE'
        and c.status = :status
        AND (:className IS NULL OR sc.name LIKE CONCAT('%', :className, '%'))
        AND (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
        AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%'))
        AND (:diplomaNumber IS NULL OR c.diploma_number LIKE CONCAT('%', :diplomaNumber, '%'))
        ORDER BY c.updated_at DESC   
        """, nativeQuery = true)
    long countCertificatesOfDepartmentAndStatus(@Param("departmentId") Long departmentId,
                                                @Param("className") String className,
                                                @Param("studentCode") String studentCode,
                                                @Param("studentName") String studentName,
                                                @Param("diplomaNumber") String diplomaNumber,
                                                @Param("status") String status);

    //danh sách ch chỉ status của 1 khoa
    @Query(value = """
            select c.* from certificates c
            join students s on c.student_id = s.id
            join student_class sc on s.student_class_id= sc.id
            join departments d on sc.department_id = d.id
            where d.id= :departmentId
            and s.status ='ACTIVE'
            and sc.status ='ACTIVE'
            and c.status = :status
            AND (:className IS NULL OR sc.name LIKE CONCAT('%', :className, '%'))
            AND (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
            AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%'))
            AND (:diplomaNumber IS NULL OR c.diploma_number LIKE CONCAT('%', :diplomaNumber, '%'))
            ORDER BY c.updated_at DESC
            LIMIT :limit OFFSET :offset 
            """,nativeQuery = true)
    List<Certificate> listCertificateOfDepartmentAndStatus(@Param("departmentId") Long departmentId,
                                                           @Param("className") String className,
                                                           @Param("studentCode") String studentCode,
                                                           @Param("studentName") String studentName,
                                                           @Param("diplomaNumber") String diplomaNumber,
                                                           @Param("status") String status,
                                                           @Param("limit") int limit,
                                                           @Param("offset") int offset
    );

    // all chung chi ADMIN
    @Query(value = """
            select c.* from certificates c
            JOIN students s on c.student_id = s.id
            JOIN student_class sc ON s.student_class_id = sc.id
            JOIN departments d ON sc.department_id = d.id
            JOIN universitys un on d.university_id= un.id
            AND s.status = 'ACTIVE'
            AND sc.status = 'ACTIVE'
            AND d.status = 'ACTIVE'
            AND (:universityName IS NULL OR un.name LIKE CONCAT('%', :universityName, '%'))
            AND (:departmentName IS NULL OR d.name LIKE CONCAT('%', :departmentName, '%'))
            AND (:className IS NULL OR sc.name LIKE CONCAT('%', :className, '%'))
            AND (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
            AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%'))
            AND (:diplomaNumber IS NULL OR c.diploma_number LIKE CONCAT('%', :diplomaNumber, '%'))
            ORDER BY c.created_at DESC
            """, nativeQuery = true)
    List<Certificate> findByAllCertificate(
            @Param("universityName") String universityName,
            @Param("departmentName") String departmentName,
            @Param("className") String className,
            @Param("studentCode") String studentCode,
            @Param("studentName") String studentName,
            @Param("diplomaNumber") String diplomaNumber
    );

    //danh sách ch chỉ của 1 truong all
    @Query(value = """
        SELECT c.* FROM certificates c
        JOIN students s ON c.student_id = s.id
        JOIN student_class sc ON s.student_class_id = sc.id
        JOIN departments d ON sc.department_id = d.id
        JOIN universitys u ON d.university_id = u.id
        WHERE u.id = :universityId
          AND s.status = 'ACTIVE'
          AND sc.status = 'ACTIVE'
          AND d.status = 'ACTIVE'
          AND c.status NOT LIKE 'DELETED'          
          AND (:departmentName IS NULL OR d.name LIKE CONCAT('%', :departmentName, '%'))
          AND (:className IS NULL OR sc.name LIKE CONCAT('%', :className, '%'))
          AND (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
          AND (:studentName IS NULL OR LOWER(s.name) LIKE CONCAT('%', LOWER(:studentName), '%'))
          AND (:diplomaNumber IS NULL OR c.diploma_number LIKE CONCAT('%', :diplomaNumber, '%'))
        ORDER BY c.updated_at DESC
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<Certificate> findPagedCertificates(
            @Param("universityId") Long universityId,
            @Param("departmentName") String departmentName,
            @Param("className") String className,
            @Param("studentCode") String studentCode,
            @Param("studentName") String studentName,
            @Param("diplomaNumber") String diplomaNumber,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    //count số lượng ch ch all pdt
    @Query(value = """
        SELECT COUNT(*) FROM certificates c
        JOIN students s ON c.student_id = s.id
        JOIN student_class sc ON s.student_class_id = sc.id
        JOIN departments d ON sc.department_id = d.id
        JOIN universitys u ON d.university_id = u.id
        WHERE u.id = :universityId
          AND s.status = 'ACTIVE'
          AND sc.status = 'ACTIVE'
          AND d.status = 'ACTIVE'
          AND c.status NOT LIKE 'DELETED'
          AND (:departmentName IS NULL OR d.name LIKE CONCAT('%', :departmentName, '%'))
          AND (:className IS NULL OR sc.name LIKE CONCAT('%', :className, '%'))
          AND (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
          AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%'))
          AND (:diplomaNumber IS NULL OR c.diploma_number LIKE CONCAT('%', :diplomaNumber, '%'))
        """, nativeQuery = true)
    long countCertificatesOfUniversity(
            @Param("universityId") Long universityId,
            @Param("departmentName") String departmentName,
            @Param("className") String className,
            @Param("studentCode") String studentCode,
            @Param("studentName") String studentName,
            @Param("diplomaNumber") String diplomaNumber
    );

    //danh sách ch chỉ của 1 truong status
    @Query(value = """
            select c.* from certificates c
            join students s on c.student_id = s.id
            join student_class sc on s.student_class_id= sc.id
            join departments d on sc.department_id = d.id
            join universitys u on d.university_id =u.id
            where u.id= :universityId
            and s.status ='ACTIVE'
            and sc.status ='ACTIVE'
            and d.status ='ACTIVE'
            and c.status = :status
            AND (:departmentName IS NULL OR d.name LIKE CONCAT('%', :departmentName, '%'))
            AND (:className IS NULL OR sc.name LIKE CONCAT('%', :className, '%'))
            AND (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
            AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%'))
            AND (:diplomaNumber IS NULL OR c.diploma_number LIKE CONCAT('%', :diplomaNumber, '%'))
            ORDER BY c.updated_at DESC
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<Certificate> listCertificateOfUniversityAndStatus(@Param("universityId") Long universityId,
                                                           @Param("departmentName") String departmentName,
                                                           @Param("className") String className,
                                                           @Param("studentCode") String studentCode,
                                                           @Param("studentName") String studentName,
                                                           @Param("diplomaNumber") String diplomaNumber,
                                                           @Param("status") String status,
                                                           @Param("limit") int limit,
                                                           @Param("offset") int offset
    );


    //đếm sl ch chỉ của 1 truong status
    @Query(value = """
            SELECT COUNT(*) FROM certificates c
            join students s on c.student_id = s.id
            join student_class sc on s.student_class_id= sc.id
            join departments d on sc.department_id = d.id
            join universitys u on d.university_id =u.id
            where u.id= :universityId
            and s.status ='ACTIVE'
            and sc.status ='ACTIVE'
            and d.status ='ACTIVE'          
            and c.status = :status
            AND (:departmentName IS NULL OR d.name LIKE CONCAT('%', :departmentName, '%'))
            AND (:className IS NULL OR sc.name LIKE CONCAT('%', :className, '%'))
            AND (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
            AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%'))
            AND (:diplomaNumber IS NULL OR c.diploma_number LIKE CONCAT('%', :diplomaNumber, '%'))
            ORDER BY c.updated_at DESC      
            """, nativeQuery = true)
    long countCertificatesOfUniversityAndStatus(@Param("universityId") Long universityId,
                                                           @Param("departmentName") String departmentName,
                                                           @Param("className") String className,
                                                           @Param("studentCode") String studentCode,
                                                           @Param("studentName") String studentName,
                                                           @Param("diplomaNumber") String diplomaNumber,
                                                           @Param("status") String status);

    Certificate findByIpfsUrl(String ipfsUrl);

    //Lấy danh sách sinh viên có chứng chỉ với id này (cho excel thêm ch ch)
    @Query(value = """
        SELECT s.student_code
        FROM certificates c
        JOIN students s ON c.student_id = s.id
        JOIN university_certificate_types uct ON c.university_certificate_type_id = uct.id
        WHERE s.id IN :studentIds AND uct.certificate_type_id = :certificateTypeId
        AND c.status IN ('PENDING', 'APPROVED')
    """, nativeQuery = true)
    List<String> findStudentCodesWithCertificateNative(@Param("studentIds") Set<Long> studentIds,
                                                       @Param("certificateTypeId") Long certificateTypeId);

    // kiểm tra xem có trùng mã chứng chỉ đã có k
    @Query(value = """
            SELECT c.diploma_number
            FROM certificates c
            WHERE c.diploma_number IN :diplomaNumbers
            AND c.status IN ('PENDING', 'APPROVED')
            """,nativeQuery = true)
    List<String> findExistingDiplomaNumbers(@Param("diplomaNumbers") Collection<String> diplomaNumbers);

    //thống kê chứng chỉ của tr theo các tháng trong năm
    @Query(value = """
    SELECT
        m.month,
        COALESCE(SUM(CASE WHEN c.status = 'PENDING' THEN 1 ELSE 0 END), 0) AS pending,
        COALESCE(SUM(CASE WHEN c.status = 'APPROVED' THEN 1 ELSE 0 END), 0) AS approved,
        COALESCE(SUM(CASE WHEN c.status = 'REJECTED' THEN 1 ELSE 0 END), 0) AS rejected
    FROM (
        SELECT 1 AS month UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
        SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL
        SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
    ) AS m
    LEFT JOIN certificates c ON MONTH(c.updated_at) = m.month AND YEAR(c.updated_at) = YEAR(CURDATE())
    LEFT JOIN students s ON c.student_id = s.id AND s.status = 'ACTIVE'
    LEFT JOIN student_class sc ON s.student_class_id = sc.id AND sc.status = 'ACTIVE'
    LEFT JOIN departments d ON sc.department_id = d.id AND d.status = 'ACTIVE'
    LEFT JOIN universitys u ON d.university_id = u.id AND u.id = :universityId
    GROUP BY m.month
    ORDER BY m.month
""", nativeQuery = true)
    List<Object[]> monthlyCertificateStatisticsOfUniversity(@Param("universityId") Long universityId);

    //thống kê chứng chỉ của khoa theo các tháng trong năm
    @Query(value = """
    SELECT
        m.month,
        COALESCE(SUM(CASE WHEN fc.status = 'PENDING' THEN 1 ELSE 0 END), 0) AS pending,
        COALESCE(SUM(CASE WHEN fc.status = 'APPROVED' THEN 1 ELSE 0 END), 0) AS approved,
        COALESCE(SUM(CASE WHEN fc.status = 'REJECTED' THEN 1 ELSE 0 END), 0) AS rejected
    FROM (
        SELECT 1 AS month UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL
        SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL
        SELECT 9 UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
    ) AS m
    LEFT JOIN (
        SELECT c.status, MONTH(c.updated_at) AS cert_month
        FROM certificates c
        JOIN students s ON c.student_id = s.id AND s.status = 'ACTIVE'
        JOIN student_class sc ON s.student_class_id = sc.id AND sc.status = 'ACTIVE'
        JOIN departments d ON sc.department_id = d.id
        WHERE d.id = :departmentId AND YEAR(c.updated_at) = YEAR(CURDATE())
    ) AS fc ON m.month = fc.cert_month
    GROUP BY m.month
    ORDER BY m.month;
    """, nativeQuery = true)
    List<Object[]> monthlyCertificateStatisticsOfDepartment(@Param("departmentId") Long departmentId);


    //thống kê sl từng loại chung chi theo tr
    @Query(value = """
           SELECT
                ct.name AS name,
                COUNT(DISTINCT c.student_id) AS approved,
                ROUND(COUNT(DISTINCT c.student_id) * 100.0 / total.total_students, 2) AS percentage
            FROM university_certificate_types uct
            JOIN certificate_types ct ON uct.certificate_type_id = ct.id
            JOIN universitys u ON uct.university_id = u.id
            LEFT JOIN certificates c ON uct.id = c.university_certificate_type_id AND c.status = 'APPROVED'
            JOIN (
                SELECT u.id AS university_id, COUNT(DISTINCT s.id) AS total_students
                FROM universitys u
                JOIN departments d ON u.id = d.university_id
                JOIN student_class sc ON d.id = sc.department_id
                JOIN students s ON sc.id = s.student_class_id
                WHERE u.id = :universityId
                and s.status ='ACTIVE'
            ) total ON total.university_id = u.id
            WHERE u.id = :universityId AND ct.status = 'ACTIVE'
            GROUP BY ct.name, total.total_students;
            """,nativeQuery = true)
    List<CountCertificateTypeRequest> countCertificateTypeOfUniversity(@Param("universityId")Long universityId);

    //thống kê sl từng loại chung chi theo khoa
    @Query(value = """
        SELECT
            ct.name,
            COUNT(DISTINCT c.student_id) AS approved,
            ROUND(COUNT(DISTINCT c.student_id) * 100.0 / total.total_students, 2) AS percentage
        FROM university_certificate_types uct
        JOIN certificate_types ct ON uct.certificate_type_id = ct.id
        JOIN universitys u ON uct.university_id = u.id
        JOIN departments d ON u.id = d.university_id
        JOIN (
            SELECT d.id AS department_id, COUNT(DISTINCT s.id) AS total_students
            FROM departments d
            JOIN student_class sc ON d.id = sc.department_id
            JOIN students s ON sc.id = s.student_class_id
            WHERE d.id = :departmentId AND s.status = 'ACTIVE'
        ) total ON total.department_id = d.id
        LEFT JOIN certificates c ON uct.id = c.university_certificate_type_id
            AND c.status = 'APPROVED'
            AND EXISTS (
                SELECT 1
                FROM students s2
                JOIN student_class sc2 ON s2.student_class_id = sc2.id
                WHERE s2.id = c.student_id
                  AND s2.status = 'ACTIVE'
                  AND sc2.department_id = d.id
            )
        WHERE ct.status = 'ACTIVE'
          AND d.id = :departmentId
        GROUP BY ct.name, total.total_students;
        """,nativeQuery = true)
    List<CountCertificateTypeRequest> countCertificateTypeOfDepartment(@Param("departmentId")Long departmentId);

    //list chung chi cua student
    @Query(value = """
            select c.* from certificates c
                        join students s on c.student_id = s.id
                        join student_class sc on s.student_class_id= sc.id
                        join departments d on sc.department_id = d.id
                        join universitys u on d.university_id =u.id
                        where s.id= :studentId
                        and s.status ='ACTIVE'
                        and sc.status ='ACTIVE'
                        and d.status ='ACTIVE'         
                        and c.status = 'APPROVED'
                        AND (:diplomaNumber IS NULL OR c.diploma_number LIKE CONCAT('%', :diplomaNumber, '%'))
                        ORDER BY c.updated_at DESC
                        LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<Certificate> certificateOfStudent(@Param("studentId") Long studentId,
                                                           @Param("diplomaNumber") String diplomaNumber,
                                                           @Param("limit") int limit,
                                                           @Param("offset") int offset
    );

    //dem sl ch ch cua sinh vien
    @Query(value = """
            SELECT COUNT(*) FROM certificates c
                        join students s on c.student_id = s.id
                        join student_class sc on s.student_class_id= sc.id
                        join departments d on sc.department_id = d.id
                        join universitys u on d.university_id =u.id
                        where s.id= :studentId
                        and s.status ='ACTIVE'
                        and sc.status ='ACTIVE'
                        and d.status ='ACTIVE'        
                        and c.status = 'APPROVED'
                        and c.status NOT LIKE 'DELETED'
                        ORDER BY c.updated_at DESC
            """, nativeQuery = true)
    long countCertificateOfStudent(@Param("studentId") Long studentId,
                                           @Param("diplomaNumber") String diplomaNumber
    );

    //kiem tra số hiệu bằng chứng chỉ
    @Query(value = """
        SELECT c.*
        FROM certificates c
        JOIN university_certificate_types uct ON c.university_certificate_type_id = uct.id
        JOIN universitys u ON uct.university_id = u.id
        WHERE u.id = :universityId
        AND c.diploma_number = :diplomaNumber
        AND c.status IN ('PENDING', 'APPROVED')
        """,nativeQuery = true)
    Certificate existByDiplomaNumber(@Param("universityId") Long universityId,
                                     @Param("diplomaNumber") String diplomaNumber
    );

    @Query(value = """
    SELECT c.*
    FROM certificates c
    JOIN university_certificate_types uct ON c.university_certificate_type_id = uct.id
    JOIN universitys u ON uct.university_id = u.id
    WHERE u.id = :universityId
      AND c.diploma_number = :diplomaNumber
      AND c.id != :certificateId
      AND c.status IN ('PENDING', 'APPROVED')
    """, nativeQuery = true)
    Certificate existByDiplomaNumberIgnoreId(
            @Param("universityId") Long universityId,
            @Param("diplomaNumber") String diplomaNumber,
            @Param("certificateId") Long certificateId
    );

    @Query(value = """
    SELECT *
    FROM certificates c
    WHERE (:status IS NULL OR c.status = :status)
    """, nativeQuery = true)
    List<Certificate> findByStatus(@Param("status") String status);

    @Modifying
    @Transactional
    @Query(value = "update certificates set status = 'DELETED' where id =:id", nativeQuery = true)
    int delete(@Param("id") Long id);
}