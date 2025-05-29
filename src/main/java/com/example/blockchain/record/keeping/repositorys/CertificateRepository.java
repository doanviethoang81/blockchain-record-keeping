package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.dtos.CertificateDTO;
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

    @Query("SELECT c FROM Certificate c WHERE c.student.id = :studentId")
    List<Certificate> findByStudentId(@Param("studentId") Long studentId);

    // all chung chi
    @Query(value = """
            select c.* from certificates c
            JOIN students s on c.student_id = s.id
            JOIN student_class sc ON s.student_class_id = sc.id
            JOIN departments d ON sc.department_id = d.id
            JOIN universitys un on d.university_id= un.id
            AND s.status = 'ACTIVE'
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
}
