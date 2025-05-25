package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.request.StudentRequest;
import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.Student;
import com.example.blockchain.record.keeping.models.StudentClass;
import com.example.blockchain.record.keeping.models.User;
import jnr.ffi.Struct;

import java.util.List;
import java.util.Optional;

public interface IStudentService {
    Student findByStudentCodeOfClass(String studentCode,Long classId);

    List<Student> getAllStudentOfUniversity(Long universityId, String departmentName, String className, String studentCode, String studentName);

    List<Student> searchStudents(Long departmentId, String className, String studentCode, String name);

    Student createStudent(StudentRequest studentRequest);

    Student findByEmailStudentCodeOfDepartment(String email, Long departmentId);
}
