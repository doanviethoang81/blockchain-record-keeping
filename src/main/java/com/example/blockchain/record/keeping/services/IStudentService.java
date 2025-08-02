package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.request.StudentRequest;
import com.example.blockchain.record.keeping.dtos.request.UpdateStudentRequest;
import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.Student;
import com.example.blockchain.record.keeping.models.StudentClass;
import com.example.blockchain.record.keeping.models.User;
import jnr.ffi.Struct;
import org.springframework.data.repository.query.Param;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IStudentService {
    Student findByStudentCodeOfUniversity(String studentCode,Long universityId);

    List<Student> getAllStudentOfUniversity(Long universityId, String departmentName, String className, String studentCode, String studentName, int limit, int offset);

    List<Student> searchStudents(Long departmentId, String className, String studentCode, String name, int limit, int offset);

    Student createStudent(StudentRequest studentRequest) throws Exception;

    Student findByEmailStudentCodeOfDepartment(String email, Long departmentId);

    Student update(Long id , UpdateStudentRequest updateStudentRequest);

    Student delete(Long id) throws Exception;

    Optional<StudentClass>  findByClassNameAndDepartmentId(Long id, String name);

    List<Student> findByStudentOfDepartment(Long departmentId, String studentCode);

    Optional<Student> findByOneStudentOfDepartment(Long departmentId, String studentCode);

    List<Student> findByStudentCodesOfDepartment(Long departmentId, Set<String> allStudentCodes);

    Student findByEmail(String email);

    boolean resetPassword(String email, String newPassword);

    long countStudentsByUniversity(Long universityId,
                                   String departmentName,
                                   String className,
                                   String studentCode,
                                   String studentName);

    long countStudentOdDepartment(
            Long departmentId,
            String className,
            String studentCode,
            String studentName);

    long countCertificateOfStudent(Long id);
    long countDegreeOfStudent(Long id);
}
