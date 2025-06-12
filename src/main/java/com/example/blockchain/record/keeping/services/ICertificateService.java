package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.request.CertificateRequest;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.Certificate;
import com.example.blockchain.record.keeping.models.Student;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ICertificateService {

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

    List<Certificate> listCertificateOfDepartment(Long departmentId,String className, String studentCode, String studentName);

    Certificate findByIdAndStatus(Long id, Status status);

    List<Certificate> listCertificateOfUniversity(Long universittyId, String departmentName,String className, String studentCode, String studentName);

    Certificate update(Certificate certificate, CertificateRequest certificateRequest);

    List<Certificate> listCertificateOfUniversityPending(Long universittyId, String departmentName, String className, String studentCode, String studentName);

    List<Certificate> listCertificateOfDepartmentPending(Long departmentId, String className, String studentCode, String studentName);

    Certificate findByIpfsUrl(String ipfsUrl);

    Map<String, Boolean> findCertificatesOfStudentsByType(Set<Long> studentIds, Long certificateTypeId);
}
