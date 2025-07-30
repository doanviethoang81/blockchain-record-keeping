package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.request.CertificateRequest;
import com.example.blockchain.record.keeping.dtos.request.CountCertificateTypeRequest;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.Certificate;
import com.example.blockchain.record.keeping.models.Student;
import com.example.blockchain.record.keeping.response.CertificateOfStudentResponse;
import com.example.blockchain.record.keeping.response.CountCertificateTypeResponse;
import com.example.blockchain.record.keeping.response.DegreeClassificationByYearResponse;
import com.example.blockchain.record.keeping.response.MonthlyCertificateStatisticsResponse;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    List<Certificate> listCertificateOfDepartment(Long departmentId,String className, String studentCode, String studentName, String diplomaNumber, int limit, int offset);

    Certificate findByIdAndStatus(Long id, Status status);

    List<Certificate> listCertificateOfUniversity(Long universittyId, String departmentName,String className, String studentCode, String studentName, String diplomaNumber, int limit, int offset);

    boolean update(Long certificateId, CertificateRequest certificateRequest);

    List<Certificate> listCertificateOfUniversityAndStatus(Long universittyId, String departmentName, String className, String studentCode, String studentName,String diplomaNumber, String status, int limit, int offset);

    List<Certificate> listCertificateOfDepartmentAndStatus(Long departmentId, String className, String studentCode, String studentName,String diplomaNumber, String status, int limit, int offset);

    Certificate findByIpfsUrl(String ipfsUrl);

    Map<String, Boolean> findCertificatesOfStudentsByType(Set<Long> studentIds, Long certificateTypeId);

    List<MonthlyCertificateStatisticsResponse> monthlyCertificateStatisticsOfUniversity(Long universityId);

    List<MonthlyCertificateStatisticsResponse> monthlyCertificateStatisticsOfDepartment(Long universityId);

    List<CountCertificateTypeResponse> countCertificateTypeOfUniversity(Long universityId);

    List<CountCertificateTypeResponse> countCertificateTypeOfDepartment(Long departmentId);

    long countCertificatesOfUniversity(Long universittyId, String departmentName, String className, String studentCode, String studentName,String diplomaNumber);

    long countCertificatesOfUniversityAndStatus(Long universittyId, String departmentName, String className, String studentCode, String studentName,String diplomaNumber, String status);

    long countCertificatesOfDepartment(Long departmentId, String className, String studentCode, String studentName,String diplomaNumber);

    long countCertificatesOfDepartmentOfStatus(Long departmentId, String className, String studentCode, String studentName,String diplomaNumber, String status);

    List<CertificateOfStudentResponse> certificateOfStudent(Long studentId, String diplomaNumber, int limit, int offset);

    long countCertificateOfStudent(Long studentId, String diplomaNumber);

    boolean existByDiplomaNumber(Long universityId, String diplomaNumber);

    boolean existByDiplomaNumberIgnoreId(Long universityId, String diplomaNumber, Long certificateId);

    List<Certificate> findByStatus(String status);

    Certificate delete(Long id);

    List<DegreeClassificationByYearResponse> getCertificateClassificationByUniversityAndLast5Years(Long universityId);

    List<DegreeClassificationByYearResponse> getCertificateClassificationByDepartmentAndLast5Years(Long universityId);


}
