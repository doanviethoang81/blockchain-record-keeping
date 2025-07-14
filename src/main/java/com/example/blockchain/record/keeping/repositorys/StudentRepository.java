package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.dtos.StudentDTO;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.Certificate;
import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.Student;
import com.example.blockchain.record.keeping.models.StudentClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface StudentRepository extends JpaRepository<Student,Long> {

    Optional<Student> findByIdAndStatus(Long id, Status status);

    Optional<Student> findByEmailAndStatus(String email, Status status);

    boolean existsBystudentCode(String studentCode);

    Optional<Student> findByStudentCode(String studentCode);

    @Query(value = """
            SELECT s.* from students s
            where s.student_class_id = :studentClassId and s.status ='ACTIVE'
            """, nativeQuery = true)
    List<Student> findByStudentClassId(@Param("studentClassId") Long studentClassId);

    // tìm sinh viên theo mssv trong 1 Trường
    @Query(value = """
    SELECT s.* FROM students s
    JOIN student_class sc ON s.student_class_id = sc.id
    Join departments d on sc.department_id = d.id
    join universitys u on d.university_id= u.id
    WHERE s.student_code = :studentCode
      AND u.id = :universityId
      AND s.status = 'ACTIVE'
    """, nativeQuery = true)
    Optional<Student> findByStudentCodeOfUniversity(@Param("studentCode") String studentCode,
                                                       @Param("universityId") Long universityId);

    //kiểm tra co trung email sv trong 1 khoa k
    @Query(value = """
    SELECT s.* FROM students s
    JOIN student_class sc ON s.student_class_id = sc.id
    JOIN departments d on sc.department_id = d.id
    JOIN universitys u on d.university_id = u.id
    WHERE s.email = :studentEmail
      AND u.id = :universityId
      AND s.status = 'ACTIVE'
      AND d.status = 'ACTIVE'
    """, nativeQuery = true)
    Optional<Student> findByEmailStudentCodeOfDepartment(@Param("studentEmail") String studentEmail,
                                               @Param("universityId") Long universityId);


    // danh sach sv theo 1 tr
    @Query(value = """
    select s.* from students s
    JOIN student_class sc on s.student_class_id = sc.id
    JOIN departments d on sc.department_id = d.id
    JOIN universitys u on d.university_id =u.id
    where u.id = :universityId and d.status='ACTIVE'
    ORDER BY s.updated_at DESC
    """, nativeQuery = true)
    List<Student> getAllStudentOfUniversity(@Param("universityId") Long universityId);

    // danh sach sv theo 1 khoa
    @Query(value = """
    SELECT s.* FROM students s
    JOIN student_class sc ON s.student_class_id = sc.id
    JOIN departments d ON sc.department_id = d.id
    WHERE d.id = :departmentId
      AND s.status = 'ACTIVE'
      AND sc.status = 'ACTIVE'
      AND d.status = 'ACTIVE'
    ORDER BY s.updated_at DESC
    """, nativeQuery = true)
    List<Student> getAllStudentOfDepartment(@Param("departmentId") Long departmentId);

    // count sách sinh viên 1 khoa
    @Query(value = """
    SELECT count(*) FROM students s
    JOIN student_class sc ON s.student_class_id = sc.id
    JOIN departments d ON sc.department_id = d.id
    WHERE (:departmentId IS NULL OR d.id = :departmentId)
      AND (:className IS NULL OR sc.name LIKE CONCAT('%', :className, '%'))
      AND (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
      AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%'))
      AND s.status = 'ACTIVE'
      and sc.status ='ACTIVE'
      AND d.status = 'ACTIVE'
      ORDER BY s.updated_at DESC
    """, nativeQuery = true)
    long countStudentOdDepartment(@Param("departmentId") Long departmentId,
                                 @Param("className") String className,
                                 @Param("studentCode") String studentCode,
                                 @Param("studentName") String studentName);

    // danh sách sinh viên 1 khoa
    @Query(value = """
    SELECT s.* FROM students s
    JOIN student_class sc ON s.student_class_id = sc.id
    JOIN departments d ON sc.department_id = d.id
    WHERE (:departmentId IS NULL OR d.id = :departmentId)
      AND (:className IS NULL OR sc.name LIKE CONCAT('%', :className, '%'))
      AND (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
      AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%'))
      AND s.status = 'ACTIVE'
      and sc.status ='ACTIVE'
      AND d.status = 'ACTIVE'
      ORDER BY s.updated_at DESC
      LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<Student> searchStudents(@Param("departmentId") Long departmentId,
                                 @Param("className") String className,
                                 @Param("studentCode") String studentCode,
                                 @Param("studentName") String studentName,
                                 @Param("limit") int limit,
                                 @Param("offset") int offset);

    //count số lượng sinh viên pdt
    @Query(value = """
    SELECT count(*) FROM students s
    JOIN student_class sc ON s.student_class_id = sc.id
    JOIN departments d ON sc.department_id = d.id
    JOIN universitys un on d.university_id= un.id
    WHERE un.id = :universityId
      AND (:departmentName IS NULL OR d.name LIKE CONCAT('%', :departmentName, '%'))
      AND (:className IS NULL OR sc.name LIKE CONCAT('%', :className, '%'))
      AND (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
      AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%'))
      AND s.status = 'ACTIVE'
      and sc.status ='ACTIVE'
      AND d.status = 'ACTIVE' 
      ORDER BY s.updated_at DESC
    """, nativeQuery = true)
    long countStudentsByUniversity(
            @Param("universityId") Long universityId,
            @Param("departmentName") String departmentName,
            @Param("className") String className,
            @Param("studentCode") String studentCode,
            @Param("studentName") String studentName);

    // tìm sv theo khoa lớp mssv tên (PDT)
    @Query(value = """
    SELECT s.* FROM students s
    JOIN student_class sc ON s.student_class_id = sc.id
    JOIN departments d ON sc.department_id = d.id
    JOIN universitys un on d.university_id= un.id
    WHERE un.id = :universityId
      AND (:departmentName IS NULL OR d.name LIKE CONCAT('%', :departmentName, '%'))
      AND (:className IS NULL OR sc.name LIKE CONCAT('%', :className, '%'))
      AND (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
      AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%'))
      AND s.status = 'ACTIVE'
      and sc.status ='ACTIVE'
      AND d.status = 'ACTIVE'
      ORDER BY s.updated_at DESC
      LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<Student> searchStudentsByUniversity(
                                 @Param("universityId") Long universityId,
                                 @Param("departmentName") String departmentName,
                                 @Param("className") String className,
                                 @Param("studentCode") String studentCode,
                                 @Param("studentName") String studentName,
                                 @Param("limit") int limit,
                                 @Param("offset") int offset);

    // tim kiem 1 list sinh vien trong khoa
    @Query(value = """
            select s.* from students s
            join student_class sc on s.student_class_id = sc.id
            join departments d on sc.department_id = d.id
            where d.id = :departmentId
            and (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
            and s.status = 'ACTIVE'
            and d.status = 'ACTIVE'
            """, nativeQuery = true)
    List<Student> findByStudentOfDepartment(
            @Param("departmentId") Long departmentId,
            @Param("studentCode") String studentCode
    );

    // tim kiem 1 sv theo khoa
    @Query(value = """
            select s.* from students s
            join student_class sc on s.student_class_id = sc.id
            join departments d on sc.department_id = d.id
            where d.id = :departmentId
            and (s.student_code LIKE CONCAT(:studentCode))
            and s.status = 'ACTIVE'
            and d.status = 'ACTIVE'
            """, nativeQuery = true)
    Optional<Student> findByOneStudentOfDepartment(
            @Param("departmentId") Long departmentId,
            @Param("studentCode") String studentCode
    );

    // tìm tất cả sinh viên theo mssv trong file excel (excel add student)
    @Query(value = """
        SELECT s.* FROM students s
        JOIN student_class sc on s.student_class_id = sc.id
        WHERE s.student_code IN :studentCodes
          AND sc.department_id = :departmentId
    """, nativeQuery = true)
    List<Student> findByStudentCodesOfDepartment(@Param("departmentId") Long departmentId,
                                                 @Param("studentCodes") Set<String> studentCodes);

    @Query(value = """
          SELECT s.student_code FROM students s WHERE s.id = :id
    """, nativeQuery = true)
    String findByStudentCode(@Param("id") Long id);


    //kiem tra sinh viên có chưứng chỉ chưa
    @Query(value = """
        SELECT COUNT(*) AS count_certificate
        FROM certificates c
        WHERE c.student_id = :id
        """,nativeQuery = true)
    long countCertificateOfStudent(@Param("id") Long id);

    //kiem tra sinh viên có văn bằng chưa
    @Query(value = """
        SELECT COUNT(*) AS count_degree
        FROM degrees d
        WHERE d.student_id = :id
        """,nativeQuery = true)
    long countDegreeOfStudent(@Param("id") Long id);
}
