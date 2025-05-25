package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.dtos.StudentDTO;
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

@Repository
public interface StudentRepository extends JpaRepository<Student,Long> {

    boolean existsBystudentCode(String studentCode);

    Optional<Student> findByStudentCode(String studentCode);

    @Query(value = """
            SELECT s.* from students s
            where s.student_class_id = :studentClassId and s.status ='ACTIVE'
            """, nativeQuery = true)
    List<Student> findByStudentClassId(@Param("studentClassId") Long studentClassId);

    // tìm sinh viên theo mssv trong 1 lớp
    @Query(value = """
    SELECT s.* FROM students s
    JOIN student_class sc ON s.student_class_id = sc.id
    WHERE s.student_code = :studentCode
      AND sc.id = :classId
      AND s.status = 'ACTIVE'
    """, nativeQuery = true)
    Optional<Student> findByStudentCodeOfClass(@Param("studentCode") String studentCode,
                                                       @Param("classId") Long classId);

    //kiểm tra co trung email sv trong 1 khoa k
    @Query(value = """
    SELECT s.* FROM students s
    JOIN student_class sc ON s.student_class_id = sc.id
    JOIN departments d on sc.department_id = d.id
    WHERE s.email = :studentEmail
      AND d.id = :departmentId
      AND s.status = 'ACTIVE'
      AND d.status = 'ACTIVE'
    """, nativeQuery = true)
    Optional<Student> findByEmailStudentCodeOfDepartment(@Param("studentEmail") String studentEmail,
                                               @Param("departmentId") Long departmentId);


    // danh sach sv theo 1 tr
    @Query(value = """
    select s.* from students s
    JOIN student_class sc on s.student_class_id = sc.id
    JOIN departments d on sc.department_id = d.id
    JOIN universitys u on d.university_id =u.id
    where u.id = :universityId and d.status='ACTIVE'
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
    ORDER BY s.created_at DESC
    """, nativeQuery = true)
    List<Student> getAllStudentOfDepartment(@Param("departmentId") Long departmentId);

    // tìm sv theo lớp mssv tên (Khoa)
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
    """, nativeQuery = true)
    List<Student> searchStudents(@Param("departmentId") Long departmentId,
                                 @Param("className") String className,
                                 @Param("studentCode") String studentCode,
                                 @Param("studentName") String studentName);

    // tìm sv theo khoa lớp mssv tên (PDT)
    @Query(value = """
    SELECT s.* FROM students s
    JOIN student_class sc ON s.student_class_id = sc.id
    JOIN departments d ON sc.department_id = d.id
    JOIN universitys un on d.university_id= un.id
    WHERE (:universityId IS NULL OR un.id = :universityId )
      AND (:departmentName IS NULL OR d.name LIKE CONCAT('%', :departmentName, '%'))
      AND (:className IS NULL OR sc.name LIKE CONCAT('%', :className, '%'))
      AND (:studentCode IS NULL OR s.student_code LIKE CONCAT('%', :studentCode, '%'))
      AND (:studentName IS NULL OR s.name LIKE CONCAT('%', :studentName, '%'))
      AND s.status = 'ACTIVE'
      and sc.status ='ACTIVE'
      AND d.status = 'ACTIVE' 
    """, nativeQuery = true)
    List<Student> searchStudentsByUniversity(
                                 @Param("universityId") Long universityId,
                                 @Param("departmentName") String departmentName,
                                 @Param("className") String className,
                                 @Param("studentCode") String studentCode,
                                 @Param("studentName") String studentName);


}
