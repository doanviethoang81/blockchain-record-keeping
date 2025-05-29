package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.CertificateDTO;
import com.example.blockchain.record.keeping.dtos.StudentDTO;
import com.example.blockchain.record.keeping.models.Certificate;
import com.example.blockchain.record.keeping.models.Student;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ICertificateService {

    Certificate saveAll(CertificateDTO certificateDTO, StudentDTO studentDTO);

    List<Certificate> listCertificateOfStudent(Student student);

    List<Certificate> findByAllCertificate(
            String universityName,
            String departmentNa,
            String className,
            String studentCode,
            String studentName
    );

    Certificate findById(Long id);

    Optional<Certificate> existingStudentOfCertificate(Long studentId, Long certificateId);

    Certificate save(Certificate certificate);
    List<Certificate> saveAll(List<Certificate> certificateList);
}
