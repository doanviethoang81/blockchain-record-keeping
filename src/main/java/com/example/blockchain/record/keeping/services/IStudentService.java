package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.Student;
import com.example.blockchain.record.keeping.models.User;
import jnr.ffi.Struct;

import java.util.List;
import java.util.Optional;

public interface IStudentService {
    List<Student> listUserOfDepartment(Department department);

    Optional<Student> findByStudentCodeAndDepartment_Id(String mssv, Long departmentId);


}
