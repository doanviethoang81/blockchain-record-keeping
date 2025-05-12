package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.dtos.CertificateDTO;
import com.example.blockchain.record.keeping.models.Certificate;
import com.example.blockchain.record.keeping.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate,Long> {
    List<Certificate> findByStudent(Student student);

    @Query("SELECT c FROM Certificate c WHERE c.student.id = :studentId")
    List<Certificate> findByStudentId(@Param("studentId") Long studentId);
}
