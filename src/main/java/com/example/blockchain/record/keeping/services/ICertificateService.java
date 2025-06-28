package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.request.CertificateRequest;
import com.example.blockchain.record.keeping.dtos.request.CountCertificateTypeRequest;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.Certificate;
import com.example.blockchain.record.keeping.models.Student;
import com.example.blockchain.record.keeping.response.CountCertificateTypeResponse;
import com.example.blockchain.record.keeping.response.MonthlyCertificateStatisticsResponse;

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
            String studentName,
            String diplomaNumber
    );

    Certificate findById(Long id);

    Optional<Certificate> existingStudentOfCertificate(Long studentId, Long certificateId);

    Certificate save(Certificate certificate);
    List<Certificate> saveAll(List<Certificate> certificateList);

    List<Certificate> listCertificateOfDepartment(Long departmentId,String className, String studentCode, String studentName, String diplomaNumber);

    Certificate findByIdAndStatus(Long id, Status status);

    List<Certificate> listCertificateOfUniversity(Long universittyId, String departmentName,String className, String studentCode, String studentName, String diplomaNumber, int limit, int offset);

    Certificate update(Certificate certificate, CertificateRequest certificateRequest);

    List<Certificate> listCertificateOfUniversityAndStatus(Long universittyId, String departmentName, String className, String studentCode, String studentName,String diplomaNumber, String status);

    List<Certificate> listCertificateOfDepartmentAndStatus(Long departmentId, String className, String studentCode, String studentName,String diplomaNumber, String status);

    Certificate findByIpfsUrl(String ipfsUrl);

    Map<String, Boolean> findCertificatesOfStudentsByType(Set<Long> studentIds, Long certificateTypeId);

    List<MonthlyCertificateStatisticsResponse> monthlyCertificateStatisticsOfUniversity(Long universityId);

    List<MonthlyCertificateStatisticsResponse> monthlyCertificateStatisticsOfDepartment(Long universityId);

    List<CountCertificateTypeResponse> countCertificateTypeOfUniversity(Long universityId);

    List<CountCertificateTypeResponse> countCertificateTypeOfDepartment(Long departmentId);

    long countCertificatesOfUniversity(Long universittyId, String departmentName, String className, String studentCode, String studentName,String diplomaNumber);
}
