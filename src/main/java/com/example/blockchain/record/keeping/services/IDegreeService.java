package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.request.DegreeRequest;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.Degree;
import com.example.blockchain.record.keeping.models.Student;
import com.example.blockchain.record.keeping.response.DegreeClassificationByYearResponse;
import com.example.blockchain.record.keeping.response.DegreeClassificationStatisticsResponse;
import com.example.blockchain.record.keeping.response.DegreeResponse;
import org.springframework.data.repository.query.Param;

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

    List<Degree> listAllDegreeOfUniversity(Long universittyId, String departmentName, String className, String studentCode, String studentName,String graduationYear, String diplomaNumber, int limit, int offset);

    List<Degree> listAllDegreeOfUniversityAndStatus(Long universittyId, String departmentName, String className, String studentCode, String studentName,String graduationYear, String diplomaNumber,String status, int limit, int offset);

    List<Degree> listAllDegreeOfDepartment(Long departmentId, String className, String studentCode, String studentName,String graduationYear, String diplomaNumber, int limit, int offset);

    List<Degree> listAllDegreeOfDepartmentAndStatus(Long departmentId, String className, String studentCode, String studentName,String graduationYear,String diplomaNumber, String status, int limit, int offset);

    List<Degree> listAllDegree(String universityName, String departmentName, String className, String studentCode, String studentName,String graduationYear, String diplomaNumber);

    Degree findByIdAndStatus(Long id, Status status);

    Degree findByIpfsUrl(String ipfsUrl);

    DegreeClassificationStatisticsResponse degreeClassificationStatisticsOfUniversity(Long universityId);

    DegreeClassificationStatisticsResponse degreeClassificationStatisticsOfDepartment(Long departmentId);

    List<DegreeClassificationByYearResponse> getDegreeClassificationByUniversityAndLast5Years(Long universityId);

    List<DegreeClassificationByYearResponse> getDegreeClassificationByDepartmentAndLast5Years(Long departmentId);

    List<DegreeResponse> degreeOfStudent(Long studentId);

    boolean existByDiplomanumber(Long universityId, String diplomaNumber);

    boolean existByLotteryNumber(Long universityId, String lotteryNumber);

    boolean existByDiplomanumberIngnoreId(Long universityId, String diplomaNumber, Long degreeId);

    boolean existByLotteryNumberIngnoreId(Long universityId, String lotteryNumber, Long degreeId);

    long countAllDegreeOfUniversity(Long universityId,
                                    String departmentName,
                                    String className,
                                    String studentCode,
                                    String studentName,
                                    String graduationYear,
                                    String diplomaNumber);

    long countAllDegreeOfUniversityAndStatus(Long universityId,
                                             String departmentName,
                                             String className,
                                             String studentCode,
                                             String studentName,
                                             String graduationYear,
                                             String diplomaNumber,
                                             String status);

    long countAllDegreeOfDepartment(Long departmentId,
                                    String className,
                                    String studentCode,
                                    String studentName,
                                    String graduationYear,
                                    String diplomaNumber);

    long countDegreeOfDepartmentAndStatus(
            Long departmentId,
            String className,
            String studentCode,
            String studentName,
            String graduationYear,
            String diplomaNumber,
            String status);

    Degree delete(Long id);
}
