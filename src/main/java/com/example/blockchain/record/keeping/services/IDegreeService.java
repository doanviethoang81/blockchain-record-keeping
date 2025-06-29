package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.request.DegreeRequest;
import com.example.blockchain.record.keeping.models.Degree;
import com.example.blockchain.record.keeping.models.Student;
import com.example.blockchain.record.keeping.response.DegreeClassificationByYearResponse;
import com.example.blockchain.record.keeping.response.DegreeClassificationStatisticsResponse;
import com.example.blockchain.record.keeping.response.DegreeResponse;

import java.util.List;
import java.util.Optional;

public interface IDegreeService {

    Degree save(Degree degree);

    boolean existsByStudent(Student student);

    Degree findById(Long id);

    boolean existsByIdAndStatus(Long id);

    Degree updateDegree(Long id,DegreeRequest degreeRequest);

    List<Degree> findAll();

    List<Degree> saveAll(List<Degree> degreeList);

    Degree findbyDiplomaNumber(String diplomaNumber);

    Degree findByLotteryNumber(String lotteryNumber);

    List<Degree> listAllDegreeOfUniversity(Long universittyId, String departmentName, String className, String studentCode, String studentName,String graduationYear, String diplomaNumber);

    List<Degree> listAllDegreeOfUniversityAndStatus(Long universittyId, String departmentName, String className, String studentCode, String studentName,String graduationYear, String diplomaNumber,String status);

    List<Degree> listAllDegreeOfDepartment(Long departmentId, String className, String studentCode, String studentName,String graduationYear, String diplomaNumber);

    List<Degree> listAllDegreeOfDepartmentAndStatus(Long departmentId, String className, String studentCode, String studentName,String graduationYear,String diplomaNumber, String status);

    List<Degree> listAllDegree(String universityName, String departmentName, String className, String studentCode, String studentName,String graduationYear, String diplomaNumber);

    Degree findByIdAndStatus(Long id);

    Degree findByIpfsUrl(String ipfsUrl);

    DegreeClassificationStatisticsResponse degreeClassificationStatisticsOfUniversity(Long universityId);

    DegreeClassificationStatisticsResponse degreeClassificationStatisticsOfDepartment(Long departmentId);

    List<DegreeClassificationByYearResponse> getDegreeClassificationByUniversityAndLast5Years(Long universityId);

    List<DegreeClassificationByYearResponse> getDegreeClassificationByDepartmentAndLast5Years(Long departmentId);

    List<DegreeResponse> degreeOfStudent(Long studentId);
}
