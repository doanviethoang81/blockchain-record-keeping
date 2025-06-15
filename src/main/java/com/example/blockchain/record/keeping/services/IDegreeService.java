package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.request.DegreeRequest;
import com.example.blockchain.record.keeping.models.Degree;
import com.example.blockchain.record.keeping.models.Student;

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

    List<Degree> listAllDegreeOfUniversity(Long universittyId, String departmentName, String className, String studentCode, String studentName,String graduationYear);

    List<Degree> listAllDegreeOfUniversityPending(Long universittyId, String departmentName, String className, String studentCode, String studentName,String graduationYear);

    List<Degree> listAllDegreeOfDepartment(Long departmentId, String className, String studentCode, String studentName,String graduationYear);

    List<Degree> listAllDegreeOfDepartmentPending(Long departmentId, String className, String studentCode, String studentName,String graduationYear);

    List<Degree> listAllDegree(String universityName, String departmentName, String className, String studentCode, String studentName,String graduationYear);
}
