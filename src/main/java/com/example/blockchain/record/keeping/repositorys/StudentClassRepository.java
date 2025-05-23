package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.models.StudentClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentClassRepository extends JpaRepository<StudentClass,Long> {

    Optional<StudentClass> findByName(String name);

    //lay ds lop theo khoa
    @Query(value = """
        SELECT sc.* FROM student_class sc
        JOIN departments d ON sc.department_id = d.id
        WHERE d.id = :departmentId
        """, nativeQuery = true)
    List<StudentClass> findAllClassesByDepartmentId(@Param("departmentId") Long departmentId);


}
