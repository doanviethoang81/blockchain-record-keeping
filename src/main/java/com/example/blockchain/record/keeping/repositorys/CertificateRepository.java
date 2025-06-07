package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.dtos.CertificateDTO;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.Certificate;
import com.example.blockchain.record.keeping.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
            AND (:className IS NULL OR sc.name LIKE CONCAT('%', :className, '%'))
            AND (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
            AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%'))
            ORDER BY c.updated_at DESC      
            """,nativeQuery = true)
    List<Certificate> listCertificateOfDepartment(@Param("departmentId") Long departmentId,
                                                  @Param("className") String className,
                                                  @Param("studentCode") String studentCode,
                                                  @Param("studentName") String studentName);

    //danh sách ch chỉ chưa được xác thực của 1 khoa
    @Query(value = """
            select c.* from certificates c
            join students s on c.student_id = s.id
            join student_class sc on s.student_class_id= sc.id
            join departments d on sc.department_id = d.id
            where d.id= :departmentId
            and s.status ='ACTIVE'
            and sc.status ='ACTIVE'      
            and c.status ='PENDING'
            AND (:className IS NULL OR sc.name LIKE CONCAT('%', :className, '%'))
            AND (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
            AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%'))
            ORDER BY c.updated_at DESC      
            """,nativeQuery = true)
    List<Certificate> listCertificateOfDepartmentPending(@Param("departmentId") Long departmentId,
                                                  @Param("className") String className,
                                                  @Param("studentCode") String studentCode,
                                                  @Param("studentName") String studentName);


    //danh sách ch chỉ của 1 truong
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
            AND (:departmentName IS NULL OR d.name LIKE CONCAT('%', :departmentName, '%'))
            AND (:className IS NULL OR sc.name LIKE CONCAT('%', :className, '%'))
            AND (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
            AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%'))
            ORDER BY c.updated_at DESC      
            """, nativeQuery = true)
    List<Certificate> listCertificateOfUniversity(@Param("universityId") Long universityId,
                                                  @Param("departmentName") String departmentName,
                                                  @Param("className") String className,
                                                  @Param("studentCode") String studentCode,
                                                  @Param("studentName") String studentName);

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
            ORDER BY c.created_at DESC
            """, nativeQuery = true)
    List<Certificate> findByAllCertificate(
            @Param("universityName") String universityName,
            @Param("departmentName") String departmentName,
            @Param("className") String className,
            @Param("studentCode") String studentCode,
            @Param("studentName") String studentName
    );

    //danh sách ch chỉ của 1 truong chưa xác thực
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
            and c.status = 'PENDING'
            AND (:departmentName IS NULL OR d.name LIKE CONCAT('%', :departmentName, '%'))
            AND (:className IS NULL OR sc.name LIKE CONCAT('%', :className, '%'))
            AND (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
            AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%'))
            ORDER BY c.updated_at DESC      
            """, nativeQuery = true)
    List<Certificate> listCertificateOfUniversityPending(@Param("universityId") Long universityId,
                                                  @Param("departmentName") String departmentName,
                                                  @Param("className") String className,
                                                  @Param("studentCode") String studentCode,
                                                  @Param("studentName") String studentName);

}
