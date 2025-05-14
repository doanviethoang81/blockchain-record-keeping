package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.models.Certificate;
import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.Student;
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

    List<Student> findByDepartment(Department department);

    @Query("SELECT s FROM Student s WHERE s.department.university.id = :universityId")
    List<Student> findByUniversityId(@Param("universityId") Long universityId);

//    @Query("""
//    SELECT sv FROM Student sv
//    WHERE sv.student_code = :student_code AND sv.department_id = :departmentId""")
//    Optional<Student> findByStudentCodeAndDepartment(@Param("mssv") String mssv, @Param("departmentId") Long departmentId);

    Optional<Student> findByStudentCodeAndDepartment_Id(String studentCode, Long departmentId);
}
