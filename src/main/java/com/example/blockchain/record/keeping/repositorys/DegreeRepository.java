package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.dtos.request.DegreeClassificationStatisticsRequest;
import com.example.blockchain.record.keeping.dtos.request.FacultyDegreeStatisticRequest;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.Degree;
import com.example.blockchain.record.keeping.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
""", nativeQuery = true)
    List<String> findStudentCodesWithDegree(@Param("studentCodes") Set<String> studentCodes);

    Optional<Degree> findByDiplomaNumber(String diplomaNumber);
    Optional<Degree> findByLotteryNumber(String lotteryNumber);

    @Query(value = """
            SELECT d.diploma_number
            FROM degrees d
            WHERE d.diploma_number IN :diplomaNumbers
            """,nativeQuery = true)
    List<String> findExistingDiplomaNumbers(@Param("diplomaNumbers") Collection<String> diplomaNumbers);

    @Query(value = """
            SELECT d.lottery_number
            FROM degrees d
            WHERE d.lottery_number IN :lotteryNumbers
            """,nativeQuery = true)
    List<String> findExistingLotteryNumbers(@Param("lotteryNumbers") Collection<String> lotteryNumbers);

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
            AND (:departmentName IS NULL OR dp.name LIKE CONCAT('%', :departmentName, '%'))
            AND (:className IS NULL OR sc.name LIKE CONCAT('%', :className, '%'))
            AND (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
            AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%'))
            AND (:graduationYear IS NULL OR d.graduation_year LIKE CONCAT('%', :graduationYear, '%'))            
            ORDER BY d.updated_at DESC 
            """,nativeQuery = true)
    List<Degree> listAllDegreeOfUniversity(@Param("universityId") Long universityId,
                                            @Param("departmentName") String departmentName,
                                           @Param("className") String className,
                                           @Param("studentCode") String studentCode,
                                           @Param("studentName") String studentName,
                                           @Param("graduationYear") String graduationYear);

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
            ORDER BY d.updated_at DESC 
            """,nativeQuery = true)
    List<Degree> listDegreeOfUniversity(@Param("universityId") Long universityId,
                                               @Param("departmentName") String departmentName,
                                               @Param("className") String className,
                                               @Param("studentCode") String studentCode,
                                               @Param("studentName") String studentName,
                                               @Param("graduationYear") String graduationYear,
                                               @Param("status") String status
    );
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
            AND (:className IS NULL OR sc.name LIKE CONCAT('%', :className, '%'))
            AND (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
            AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%'))
            AND (:graduationYear IS NULL OR d.graduation_year LIKE CONCAT('%', :graduationYear, '%'))                        
            ORDER BY d.updated_at DESC 
            """,nativeQuery = true)
    List<Degree> listAllDegreeOfDepartment(@Param("departmentId") Long departmentId,
                                           @Param("className") String className,
                                           @Param("studentCode") String studentCode,
                                           @Param("studentName") String studentName,
                                           @Param("graduationYear") String graduationYear);

    // list văn bằng chưa đc xác nhận của 1 khoa
    @Query(value = """
            SELECT d.*
            FROM degrees d
            JOIN students s on d.student_id=s.id
            JOIN student_class sc on s.student_class_id = sc.id
            join departments dp on sc.department_id = dp.id
            WHERE dp.id = :departmentId
            and d.status = :status            
            and s.status ='ACTIVE'
            and sc.status ='ACTIVE'
            AND (:className IS NULL OR sc.name LIKE CONCAT('%', :className, '%'))
            AND (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
            AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%'))
            AND (:graduationYear IS NULL OR d.graduation_year LIKE CONCAT('%', :graduationYear, '%'))            
            ORDER BY d.updated_at DESC 
            """,nativeQuery = true)
    List<Degree> listAllDegreeOfDepartmentAndStatus(@Param("departmentId") Long departmentId,
                                                  @Param("className") String className,
                                                  @Param("studentCode") String studentCode,
                                                  @Param("studentName") String studentName,
                                                  @Param("graduationYear") String graduationYear,
                                                  @Param("status") String status
    );

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
            AND (:universityName IS NULL OR u.name LIKE CONCAT('%', :universityName, '%'))
            AND (:departmentName IS NULL OR dp.name LIKE CONCAT('%', :departmentName, '%'))
            AND (:className IS NULL OR sc.name LIKE CONCAT('%', :className, '%'))
            AND (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
            AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%'))
            AND (:graduationYear IS NULL OR d.graduation_year LIKE CONCAT('%', :graduationYear, '%'))          
            ORDER BY d.updated_at DESC
            """,nativeQuery = true)
    List<Degree> listAllDegree(@Param("universityName") String universityName,
                                           @Param("departmentName") String departmentName,
                                           @Param("className") String className,
                                           @Param("studentCode") String studentCode,
                                           @Param("studentName") String studentName,
                                           @Param("graduationYear") String graduationYear);

    //kiem tra xem chung chi da duoc xac thuc chua
    Degree findByIdAndStatus(Long id, Status status);


    // thống kee sô luong hvan bang theo các khoa cua 1 truong
    @Query(value = """
    SELECT dp.name AS department_name,
           COUNT(CASE WHEN d.status = 'PENDING' THEN 1 END) AS degree_pending,
           COUNT(CASE WHEN d.status = 'APPROVED' THEN 1 END) AS degree_approved,
           COUNT(CASE WHEN d.status = 'REJECTED' THEN 1 END) AS degree_rejected,
           COUNT(CASE WHEN c.status = 'PENDING' THEN 1 END) AS certificate_peding,
           COUNT(CASE WHEN c.status = 'APPROVED' THEN 1 END) AS certificate_approved,
           COUNT(CASE WHEN c.status = 'REJECTED' THEN 1 END) AS certificate_rejected
    FROM departments dp
    JOIN universitys u ON dp.university_id = u.id
    LEFT JOIN student_class sc ON sc.department_id = dp.id
    LEFT JOIN students s ON s.student_class_id = sc.id
    LEFT JOIN degrees d ON d.student_id = s.id
    LEFT JOIN certificates c on s.id = c.student_id
    WHERE u.id = :universityId
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
            """,nativeQuery = true)
    DegreeClassificationStatisticsRequest getDegreeClassificationStatisticsOfDepartment(@Param("departmentId")Long departmentId);


}
