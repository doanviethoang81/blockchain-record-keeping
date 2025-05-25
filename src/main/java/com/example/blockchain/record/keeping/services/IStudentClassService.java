package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.StudentClass;

import java.util.List;
import java.util.Optional;

public interface IStudentClassService {
    StudentClass findById(Long id);
    StudentClass findByName(String name);

    List<StudentClass>  findAllClassesByDepartmentId(Long id,String name);

    List<Department> findAllDeparmentOfUniversity(Long id);

    boolean existsByNameAndDepartmentIdAndStatus(String name, Department department);

    StudentClass save(StudentClass studentClass);

    StudentClass deleteStudentClass(Long id);

    List<StudentClass> searchNameClass(String name);
}
