package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.request.StudentRequest;
import com.example.blockchain.record.keeping.dtos.request.UpdateStudentRequest;
import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.Student;
import com.example.blockchain.record.keeping.models.StudentClass;
import com.example.blockchain.record.keeping.models.User;
import jnr.ffi.Struct;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface IStudentService {
    Student findByStudentCodeOfUniversity(String studentCode,Long universityId);

    List<Student> getAllStudentOfUniversity(Long universityId, String departmentName, String className, String studentCode, String studentName);

    List<Student> searchStudents(Long departmentId, String className, String studentCode, String name);

    Student createStudent(StudentRequest studentRequest);

    Student findByEmailStudentCodeOfDepartment(String email, Long departmentId);

    Student update(Long id , UpdateStudentRequest updateStudentRequest);

    Student delete(Long id);

    Optional<StudentClass>  findByClassNameAndDepartmentId(Long id, String name);

    List<Student> findByStudentOfDepartment(Long departmentId, String studentCode);

    Optional<Student> findByOneStudentOfDepartment(Long departmentId, String studentCode);

    List<Student> findByStudentCodesOfDepartment(Long departmentId, Set<String> allStudentCodes);

    Student findByEmail(String email);
}
