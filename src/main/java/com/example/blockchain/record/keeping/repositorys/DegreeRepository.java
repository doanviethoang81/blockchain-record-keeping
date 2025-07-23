package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.dtos.request.DegreeClassificationStatisticsRequest;
import com.example.blockchain.record.keeping.dtos.request.FacultyDegreeStatisticRequest;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.Certificate;
import com.example.blockchain.record.keeping.models.Degree;
import com.example.blockchain.record.keeping.models.Student;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface DegreeRepository extends JpaRepository<Degree,Long> {
    boolean existsByStudentAndStatusNot(Student student, Status status);
    boolean existsByIdAndStatus(Long id, Status status);

    // list mssv thêm van bang excel
    @Query(value = """
    SELECT s.student_code
    FROM degrees d
    JOIN students s ON d.student_id = s.id
    WHERE s.student_code IN :studentCodes
    AND d.status IN ('PENDING', 'APPROVED')
""", nativeQuery = true)
    List<String> findStudentCodesWithDegree(@Param("studentCodes") Set<String> studentCodes);

    Optional<Degree> findByDiplomaNumber(String diplomaNumber);
    Optional<Degree> findByLotteryNumber(String lotteryNumber);

    @Query(value = """
            SELECT d.diploma_number
            FROM degrees d
            WHERE d.diploma_number IN :diplomaNumbers
            AND d.status IN ('PENDING', 'APPROVED')
            """,nativeQuery = true)
    List<String> findExistingDiplomaNumbers(@Param("diplomaNumbers") Collection<String> diplomaNumbers);

    @Query(value = """
            SELECT d.lottery_number
            FROM degrees d
            WHERE d.lottery_number IN :lotteryNumbers
            AND d.status IN ('PENDING', 'APPROVED')
            """,nativeQuery = true)
    List<String> findExistingLotteryNumbers(@Param("lotteryNumbers") Collection<String> lotteryNumbers);

    //đếm văn bằng của 1 trường
    @Query(value = """
            SELECT count(*)
            FROM degrees d
            JOIN students s on d.student_id=s.id
            JOIN student_class sc on s.student_class_id = sc.id
            join departments dp on sc.department_id = dp.id
            WHERE dp.university_id = :universityId
            and s.status ='ACTIVE'
            and sc.status ='ACTIVE'
            and dp.status ='ACTIVE'
            and d.status NOT LIKE 'DELETED'
            AND (:departmentName IS NULL OR dp.name LIKE CONCAT('%', :departmentName, '%'))
            AND (:className IS NULL OR sc.name LIKE CONCAT('%', :className, '%'))
            AND (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
            AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%'))
            AND (:graduationYear IS NULL OR d.graduation_year LIKE CONCAT('%', :graduationYear, '%'))            
            AND (:diplomaNumber IS NULL OR d.diploma_number LIKE CONCAT('%', :diplomaNumber, '%'))            
            ORDER BY d.updated_at DESC 
            """,nativeQuery = true)
    long countAllDegreeOfUniversity(@Param("universityId") Long universityId,
                                    @Param("departmentName") String departmentName,
                                    @Param("className") String className,
                                    @Param("studentCode") String studentCode,
                                    @Param("studentName") String studentName,
                                    @Param("graduationYear") String graduationYear,
                                    @Param("diplomaNumber") String diplomaNumber);

    // list văn bằng của 1 trường
    @Query(value = """
            SELECT d.*
            FROM degrees d
            JOIN students s on d.student_id=s.id
            JOIN student_class sc on s.student_class_id = sc.id
            join departments dp on sc.department_id = dp.id
            WHERE dp.university_id = :universityId
            and s.status ='ACTIVE'
            and sc.status ='ACTIVE'
            and dp.status ='ACTIVE'
            and d.status NOT LIKE 'DELETED'
            AND (:departmentName IS NULL OR dp.name LIKE CONCAT('%', :departmentName, '%'))
            AND (:className IS NULL OR sc.name LIKE CONCAT('%', :className, '%'))
            AND (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
            AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%'))
            AND (:graduationYear IS NULL OR d.graduation_year LIKE CONCAT('%', :graduationYear, '%'))            
            AND (:diplomaNumber IS NULL OR d.diploma_number LIKE CONCAT('%', :diplomaNumber, '%'))            
            ORDER BY d.updated_at DESC 
            LIMIT :limit OFFSET :offset            
            """,nativeQuery = true)
    List<Degree> listAllDegreeOfUniversity(@Param("universityId") Long universityId,
                                           @Param("departmentName") String departmentName,
                                           @Param("className") String className,
                                           @Param("studentCode") String studentCode,
                                           @Param("studentName") String studentName,
                                           @Param("graduationYear") String graduationYear,
                                           @Param("diplomaNumber") String diplomaNumber,
                                           @Param("limit") int limit,
                                           @Param("offset") int offset);

    // đếm văn bằng theo status của 1 trường
    @Query(value = """
            SELECT count(*)
            FROM degrees d
            JOIN students s on d.student_id=s.id
            JOIN student_class sc on s.student_class_id = sc.id
            join departments dp on sc.department_id = dp.id
            WHERE dp.university_id = :universityId
            and s.status ='ACTIVE'
            and sc.status ='ACTIVE'
            and dp.status ='ACTIVE'
            and d.status = :status
            AND (:departmentName IS NULL OR dp.name LIKE CONCAT('%', :departmentName, '%'))
            AND (:className IS NULL OR sc.name LIKE CONCAT('%', :className, '%'))
            AND (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
            AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%'))
            AND (:graduationYear IS NULL OR d.graduation_year LIKE CONCAT('%', :graduationYear, '%'))                        
            AND (:diplomaNumber IS NULL OR d.diploma_number LIKE CONCAT('%', :diplomaNumber, '%'))                        
            ORDER BY d.updated_at DESC 
            """,nativeQuery = true)
    long countDegreeOfUniversityAndStatus(@Param("universityId") Long universityId,
                                          @Param("departmentName") String departmentName,
                                          @Param("className") String className,
                                          @Param("studentCode") String studentCode,
                                          @Param("studentName") String studentName,
                                          @Param("graduationYear") String graduationYear,
                                          @Param("diplomaNumber") String diplomaNumber,
                                          @Param("status") String status
    );

    // list văn bằng chưa đc xác nhận của 1 trường
    @Query(value = """
            SELECT d.*
            FROM degrees d
            JOIN students s on d.student_id=s.id
            JOIN student_class sc on s.student_class_id = sc.id
            join departments dp on sc.department_id = dp.id
            WHERE dp.university_id = :universityId
            and s.status ='ACTIVE'
            and sc.status ='ACTIVE'
            and dp.status ='ACTIVE'
            and d.status = :status
            AND (:departmentName IS NULL OR dp.name LIKE CONCAT('%', :departmentName, '%'))
            AND (:className IS NULL OR sc.name LIKE CONCAT('%', :className, '%'))
            AND (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
            AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%'))
            AND (:graduationYear IS NULL OR d.graduation_year LIKE CONCAT('%', :graduationYear, '%'))                        
            AND (:diplomaNumber IS NULL OR d.diploma_number LIKE CONCAT('%', :diplomaNumber, '%'))                        
            ORDER BY d.updated_at DESC
            LIMIT :limit OFFSET :offset            
            """,nativeQuery = true)
    List<Degree> listDegreeOfUniversityAndStatus(@Param("universityId") Long universityId,
                                                 @Param("departmentName") String departmentName,
                                                 @Param("className") String className,
                                                 @Param("studentCode") String studentCode,
                                                 @Param("studentName") String studentName,
                                                 @Param("graduationYear") String graduationYear,
                                                 @Param("diplomaNumber") String diplomaNumber,
                                                 @Param("status") String status,
                                                 @Param("limit") int limit,
                                                 @Param("offset") int offset);
    //count văn bằng của 1 khoa
    @Query(value = """
            SELECT COUNT(*)
            FROM degrees d
            JOIN students s on d.student_id=s.id
            JOIN student_class sc on s.student_class_id = sc.id
            join departments dp on sc.department_id = dp.id
            WHERE dp.id = :departmentId
            and s.status ='ACTIVE'
            and sc.status ='ACTIVE'
            and d.status NOT LIKE 'DELETED'
            AND (:className IS NULL OR sc.name LIKE CONCAT('%', :className, '%'))
            AND (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
            AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%'))
            AND (:graduationYear IS NULL OR d.graduation_year LIKE CONCAT('%', :graduationYear, '%'))
            AND (:diplomaNumber IS NULL OR d.diploma_number LIKE CONCAT('%', :diplomaNumber, '%'))
            ORDER BY d.updated_at DESC 
            """,nativeQuery = true)
    long countAllDegreeOfDepartment(@Param("departmentId") Long departmentId,
                                    @Param("className") String className,
                                    @Param("studentCode") String studentCode,
                                    @Param("studentName") String studentName,
                                    @Param("graduationYear") String graduationYear,
                                    @Param("diplomaNumber") String diplomaNumber);


    // list văn bằng của 1 khoa
    @Query(value = """
            SELECT d.*
            FROM degrees d
            JOIN students s on d.student_id=s.id
            JOIN student_class sc on s.student_class_id = sc.id
            join departments dp on sc.department_id = dp.id
            WHERE dp.id = :departmentId
            and s.status ='ACTIVE'
            and sc.status ='ACTIVE'
            and d.status NOT LIKE 'DELETED'
            AND (:className IS NULL OR sc.name LIKE CONCAT('%', :className, '%'))
            AND (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
            AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%'))
            AND (:graduationYear IS NULL OR d.graduation_year LIKE CONCAT('%', :graduationYear, '%'))
            AND (:diplomaNumber IS NULL OR d.diploma_number LIKE CONCAT('%', :diplomaNumber, '%'))
            ORDER BY d.updated_at DESC
            LIMIT :limit OFFSET :offset
            """,nativeQuery = true)
    List<Degree> listAllDegreeOfDepartment(@Param("departmentId") Long departmentId,
                                           @Param("className") String className,
                                           @Param("studentCode") String studentCode,
                                           @Param("studentName") String studentName,
                                           @Param("graduationYear") String graduationYear,
                                           @Param("diplomaNumber") String diplomaNumber,
                                           @Param("limit") int limit,
                                           @Param("offset") int offset);

    //COUNT SỐ luong văn bang theo status
    @Query(value = """
            SELECT count(*)
            FROM degrees d
            JOIN students s on d.student_id=s.id
            JOIN student_class sc on s.student_class_id = sc.id
            join departments dp on sc.department_id = dp.id
            WHERE dp.id = :departmentId
            and d.status = :status
            and sc.status ='ACTIVE'
            AND (:className IS NULL OR sc.name LIKE CONCAT('%', :className, '%'))
            AND (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
            AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%'))
            AND (:graduationYear IS NULL OR d.graduation_year LIKE CONCAT('%', :graduationYear, '%'))
            AND (:diplomaNumber IS NULL OR d.diploma_number LIKE CONCAT('%', :diplomaNumber, '%'))            
            ORDER BY d.updated_at DESC
            """,nativeQuery = true)
    long countDegreeOfDepartmentAndStatus(@Param("departmentId") Long departmentId,
                                          @Param("className") String className,
                                          @Param("studentCode") String studentCode,
                                          @Param("studentName") String studentName,
                                          @Param("graduationYear") String graduationYear,
                                          @Param("diplomaNumber") String diplomaNumber,
                                          @Param("status") String status);

    // list văn bằng chưa đc xác nhận của 1 khoa
    @Query(value = """
            SELECT d.*
            FROM degrees d
            JOIN students s on d.student_id=s.id
            JOIN student_class sc on s.student_class_id = sc.id
            join departments dp on sc.department_id = dp.id
            WHERE dp.id = :departmentId
            and d.status = :status
            and sc.status ='ACTIVE'
            AND (:className IS NULL OR sc.name LIKE CONCAT('%', :className, '%'))
            AND (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
            AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%'))
            AND (:graduationYear IS NULL OR d.graduation_year LIKE CONCAT('%', :graduationYear, '%'))
            AND (:diplomaNumber IS NULL OR d.diploma_number LIKE CONCAT('%', :diplomaNumber, '%'))
            ORDER BY d.updated_at DESC
            LIMIT :limit OFFSET :offset
            """,nativeQuery = true)
    List<Degree> listAllDegreeOfDepartmentAndStatus(@Param("departmentId") Long departmentId,
                                                    @Param("className") String className,
                                                    @Param("studentCode") String studentCode,
                                                    @Param("studentName") String studentName,
                                                    @Param("graduationYear") String graduationYear,
                                                    @Param("diplomaNumber") String diplomaNumber,
                                                    @Param("status") String status,
                                                    @Param("limit") int limit,
                                                    @Param("offset") int offset);

    // list văn bằng all admin
    @Query(value = """
            SELECT d.*
            FROM degrees d
            JOIN students s on d.student_id=s.id
            JOIN student_class sc on s.student_class_id = sc.id
            join departments dp on sc.department_id = dp.id
            join universitys u on dp.university_id = u.id
            WHERE s.status ='ACTIVE'
            and sc.status ='ACTIVE'
            and dp.status ='ACTIVE'         
            and d.status NOT LIKE 'DELETED'
            AND (:universityName IS NULL OR u.name LIKE CONCAT('%', :universityName, '%'))
            AND (:departmentName IS NULL OR dp.name LIKE CONCAT('%', :departmentName, '%'))
            AND (:className IS NULL OR sc.name LIKE CONCAT('%', :className, '%'))
            AND (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
            AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%'))
            AND (:graduationYear IS NULL OR d.graduation_year LIKE CONCAT('%', :graduationYear, '%'))          
            AND (:diplomaNumber IS NULL OR d.diploma_number LIKE CONCAT('%', :diplomaNumber, '%'))          
            ORDER BY d.updated_at DESC
            """,nativeQuery = true)
    List<Degree> listAllDegree(@Param("universityName") String universityName,
                               @Param("departmentName") String departmentName,
                               @Param("className") String className,
                               @Param("studentCode") String studentCode,
                               @Param("studentName") String studentName,
                               @Param("graduationYear") String graduationYear,
                               @Param("diplomaNumber") String diplomaNumber);

    //kiem tra xem chung chi da duoc xac thuc chua
    Degree findByIdAndStatus(Long id, Status status);


    // thống kee sô luong hvan bang theo các khoa cua 1 truong
    @Query(value = """
    SELECT dp.name AS department_name,
           COUNT(DISTINCT CASE WHEN d.status = 'PENDING' THEN d.id END) AS degree_pending,
           COUNT(DISTINCT CASE WHEN d.status = 'APPROVED' THEN d.id END) AS degree_approved,
           COUNT(DISTINCT CASE WHEN d.status = 'REJECTED' THEN d.id END) AS degree_rejected,
           COUNT(DISTINCT CASE WHEN c.status = 'PENDING' THEN c.id END) AS certificate_peding,
           COUNT(DISTINCT CASE WHEN c.status = 'APPROVED' THEN c.id END) AS certificate_approved,
           COUNT(DISTINCT CASE WHEN c.status = 'REJECTED' THEN c.id END) AS certificate_rejected
    FROM departments dp
    JOIN universitys u ON dp.university_id = u.id
    LEFT JOIN student_class sc ON sc.department_id = dp.id
    LEFT JOIN students s ON s.student_class_id = sc.id
    LEFT JOIN degrees d ON d.student_id = s.id
    LEFT JOIN certificates c on s.id = c.student_id
    WHERE u.id = :universityId
    AND dp.status NOT LIKE 'DELETED'
    GROUP BY dp.id, dp.name
    """, nativeQuery = true)
    List<FacultyDegreeStatisticRequest> getFacultyDegreeStatistics(@Param("universityId") Long universityId);

    Degree findByIpfsUrl(String ipfs);

    //thống kê văn bằng theo xếp loại của 1 trường (sx,g,k,tb)
    @Query(value = """
            SELECT
                COUNT(CASE WHEN d.rating_id = 1 THEN 1 END) AS excellent,
                COUNT(CASE WHEN d.rating_id = 2 THEN 1 END) AS veryGood,
                COUNT(CASE WHEN d.rating_id = 3 THEN 1 END) AS good,
                COUNT(CASE WHEN d.rating_id = 4 THEN 1 END) AS average
            FROM degrees d
            join students s on d.student_id = s.id
            JOIN student_class sc on s.student_class_id = sc.id
            join departments dp on sc.department_id = dp.id
            join universitys u on dp.university_id = u.id
            where u.id = :universityId
            and d.status = 'APPROVED'
            """,nativeQuery = true)
    DegreeClassificationStatisticsRequest getDegreeClassificationStatistics(@Param("universityId")Long universityId);

    //thống kê văn bằng theo xếp loại của 1 khoa (sx,g,k,tb)
    @Query(value = """
            SELECT
                COUNT(CASE WHEN d.rating_id = 1 THEN 1 END) AS excellent,
                COUNT(CASE WHEN d.rating_id = 2 THEN 1 END) AS veryGood,
                COUNT(CASE WHEN d.rating_id = 3 THEN 1 END) AS good,
                COUNT(CASE WHEN d.rating_id = 4 THEN 1 END) AS average
            FROM degrees d
            join students s on d.student_id = s.id
            JOIN student_class sc on s.student_class_id = sc.id
            join departments dp on sc.department_id = dp.id
            where dp.id = :departmentId
            and d.status = 'APPROVED'
            """,nativeQuery = true)
    DegreeClassificationStatisticsRequest getDegreeClassificationStatisticsOfDepartment(@Param("departmentId")Long departmentId);


    //thống kê văn bằng trong 5 năm của 1 trường
    @Query(value = """
            SELECT
                y.year,
                COALESCE(SUM(CASE WHEN d.status = 'PENDING' THEN 1 ELSE 0 END), 0) AS pending,
                COALESCE(SUM(CASE WHEN d.status = 'APPROVED' THEN 1 ELSE 0 END), 0) AS approved,
                COALESCE(SUM(CASE WHEN d.status = 'REJECTED' THEN 1 ELSE 0 END), 0) AS rejected
            FROM (
                SELECT YEAR(CURDATE()) AS year
                UNION ALL SELECT YEAR(CURDATE()) - 1
                UNION ALL SELECT YEAR(CURDATE()) - 2
                UNION ALL SELECT YEAR(CURDATE()) - 3
                UNION ALL SELECT YEAR(CURDATE()) - 4
            ) AS y
            LEFT JOIN degrees d ON YEAR(d.updated_at) = y.year
            LEFT JOIN students s ON d.student_id = s.id
            LEFT JOIN student_class sc ON s.student_class_id = sc.id
            LEFT JOIN departments dp ON sc.department_id = dp.id
            LEFT JOIN universitys u ON dp.university_id = u.id AND u.id = :universityId
            GROUP BY y.year
            ORDER BY y.year;
            """,nativeQuery = true)
    List<Object[]> getDegreeClassificationByUniversityAndLast5Years(@Param("universityId") Long universityId);


    //thống kê văn bằng trong 5 năm của 1 trường
    @Query(value = """
        SELECT
            y.year,
            COALESCE(SUM(CASE WHEN d.status = 'PENDING' AND dp.id = :departmentId THEN 1 ELSE 0 END), 0) AS pending,
            COALESCE(SUM(CASE WHEN d.status = 'APPROVED' AND dp.id = :departmentId THEN 1 ELSE 0 END), 0) AS approved,
            COALESCE(SUM(CASE WHEN d.status = 'REJECTED' AND dp.id = :departmentId THEN 1 ELSE 0 END), 0) AS rejected
        FROM (
            SELECT YEAR(CURDATE()) AS year
            UNION ALL SELECT YEAR(CURDATE()) - 1
            UNION ALL SELECT YEAR(CURDATE()) - 2
            UNION ALL SELECT YEAR(CURDATE()) - 3
            UNION ALL SELECT YEAR(CURDATE()) - 4
        ) AS y
        LEFT JOIN degrees d ON YEAR(d.updated_at) = y.year
        LEFT JOIN students s ON d.student_id = s.id
        LEFT JOIN student_class sc ON s.student_class_id = sc.id
        LEFT JOIN departments dp ON sc.department_id = dp.id
        GROUP BY y.year
        ORDER BY y.year;
        """,nativeQuery = true)
    List<Object[]> getDegreeClassificationByDepartmentAndLast5Years(@Param("departmentId") Long departmentId);

    // văn bằng của sinh viên
    @Query(value = """
            SELECT d.*
            FROM degrees d
            join students s on d.student_id= s.id
            WHERE d.status = 'APPROVED'
            and s.id = :studentId
            and d.status NOT LIKE 'DELETED'
            ORDER BY d.updated_at DESC
            """,nativeQuery = true)
    Optional<Degree> degreeOfStudent(@Param("studentId") Long studentId);

    //kiem tra số hiệu bằng văn bằng
    @Query(value = """
        SELECT d.*
        FROM degrees d
        JOIN students s on d.student_id = s.id
        Join student_class sc on s.student_class_id = sc.id
        join departments dp on sc.department_id = dp.id
        join universitys u ON dp.university_id = u.id
        WHERE u.id = :universityId
        AND d.diploma_number = :diplomaNumber
        AND (d.status LIKE 'APPROVED' OR d.status LIKE 'PENDING');
        """,nativeQuery = true)
    Degree existByDiplomaNumber(@Param("universityId") Long universityId,
                                @Param("diplomaNumber") String diplomaNumber
    );

    //kiem tra số vào sổ văn bằng
    @Query(value = """
        SELECT d.*
        FROM degrees d
        JOIN students s on d.student_id = s.id
        Join student_class sc on s.student_class_id = sc.id
        join departments dp on sc.department_id = dp.id
        join universitys u ON dp.university_id = u.id
        WHERE u.id = :universityId
        AND d.lottery_number = :lotteryNumber
        AND (d.status LIKE 'APPROVED' OR d.status LIKE 'PENDING');
        """,nativeQuery = true)
    Degree existByLotteryNumber(@Param("universityId") Long universityId,
                                @Param("lotteryNumber") String lotteryNumber
    );

    //kiem tra de update k lấy id
    @Query(value = """
    SELECT d.*
    FROM degrees d
    JOIN students s ON d.student_id = s.id
    JOIN student_class sc ON s.student_class_id = sc.id
    JOIN departments dp ON sc.department_id = dp.id
    JOIN universitys u ON dp.university_id = u.id
    WHERE u.id = :universityId
      AND d.diploma_number = :diplomaNumber
      AND d.id != :degreeId
      AND (d.status = 'APPROVED' OR d.status = 'PENDING')
    """, nativeQuery = true)
    Degree existByDiplomaNumberIgnoreId(@Param("universityId") Long universityId,
                                        @Param("diplomaNumber") String diplomaNumber,
                                        @Param("degreeId") Long degreeId);

    //kiem tra de update k lấy id
    @Query(value = """
    SELECT d.*
    FROM degrees d
    JOIN students s ON d.student_id = s.id
    JOIN student_class sc ON s.student_class_id = sc.id
    JOIN departments dp ON sc.department_id = dp.id
    JOIN universitys u ON dp.university_id = u.id
    WHERE u.id = :universityId
      AND d.lottery_number = :lotteryNumber
      AND d.id != :degreeId
      AND (d.status = 'APPROVED' OR d.status = 'PENDING')
    """, nativeQuery = true)
    Degree existByLotteryNumberIgnoreId(@Param("universityId") Long universityId,
                                        @Param("lotteryNumber") String lotteryNumber,
                                        @Param("degreeId") Long degreeId);

    //xuat excel
    @Query(value = """
    SELECT *
    FROM degrees d
    WHERE (:status IS NULL OR d.status = :status)
    """, nativeQuery = true)
    List<Degree> findByStatus(@Param("status") String status);

    @Modifying
    @Transactional
    @Query(value = "update degrees set status = 'DELETED' where id =:id", nativeQuery = true)
    int delete(@Param("id") Long id);

    // khoa có nhiều văn bằng nhất (pdt)
    @Query(value = """
            SELECT dp.id AS departmentId, dp.name AS departmentName, COUNT(d.id) AS total
                    FROM departments dp
                    LEFT JOIN student_class sc ON sc.department_id = dp.id
                    LEFT JOIN students s ON s.student_class_id = sc.id
                    LEFT JOIN degrees d ON d.student_id = s.id AND d.status = 'APPROVED'
                    WHERE dp.university_id = :universityId
                    GROUP BY dp.id, dp.name
                    ORDER BY total DESC
                    LIMIT 1;
            """,nativeQuery = true)
    Map<String, Object> getTopDepartmentWithMostDegrees(@Param("universityId") Long universityId);


    //lớp có nhiều văn bằng nhất
    @Query(value = """
            SELECT sc.id AS classId,
                           sc.name AS className,
                           COUNT(c.id) AS total
                    FROM student_class sc
                    JOIN departments d ON sc.department_id = d.id
                    LEFT JOIN students s ON s.student_class_id = sc.id
                    LEFT JOIN certificates c ON c.student_id = s.id AND c.status = 'APPROVED'
                    WHERE d.university_id = :universityId
                    GROUP BY sc.id, sc.name
                    ORDER BY total DESC
                    LIMIT 1;
            """,nativeQuery = true)
    Map<String, Object> getTopClassWithMostDegrees(@Param("universityId") Long universityId);

    //lớp có nhiều văn bằng nhất (khoa)
    @Query(value = """
            SELECT sc.id AS classId, sc.name AS className,
            COUNT(sc.id) AS total
            FROM departments d
            join student_class sc on d.id = sc.department_id
            join students s on sc.id = s.student_class_id
            join degrees dg on s.id = dg.student_id
            WHERE d.id = :departmentId
            and dg.status = 'APPROVED'
            GROUP BY sc.id, sc.name
            ORDER BY total DESC
            LIMIT 1
            """,nativeQuery = true)
    Map<String, Object> getTopClassWithMostDegreesOfDepartment(@Param("departmentId") Long departmentId);




}
