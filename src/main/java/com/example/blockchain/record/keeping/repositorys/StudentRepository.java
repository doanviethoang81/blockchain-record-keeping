package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student,Long> {

    boolean existsBystudentCode(String studentCode);

    Optional<Student> findByStudentCode(String studentCode);
}
