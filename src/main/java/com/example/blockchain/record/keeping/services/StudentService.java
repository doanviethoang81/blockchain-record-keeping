package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.CertificateDTO;
import com.example.blockchain.record.keeping.dtos.DegreeDTO;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.CertificateRepository;
import com.example.blockchain.record.keeping.repositorys.StudentRepository;
import com.example.blockchain.record.keeping.response.ClassWithStudentsResponse;
import com.example.blockchain.record.keeping.response.DepartmentWithClassWithStudentResponse;
import com.example.blockchain.record.keeping.response.StudentDetailReponse;
import com.example.blockchain.record.keeping.response.StudentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService implements IStudentService{
    private final StudentRepository studentRepository;
    private final CertificateService certificateService;
    private final CertificateRepository certificateRepository;
    private final DegreeService degreeService;
    private final StudentClassService studentClassService;
    private final DepartmentService departmentService;

    // danh sách sv của các lớp của 1 khoa
    public List<Student> studentOfClassOfDepartmentList(Long idDepartment){
        List<Student> studentList = studentRepository.getAllStudentOfDepartment(idDepartment);
         return  studentList;
    }

    @Override
    public Optional<Student> findByStudentCodeAndDepartmentId(String studentCode, Long departmentId) {
        return studentRepository.findByStudentCodeAndDepartmentId(studentCode,departmentId);
    }

    @Override
    public List<Student> getAllStudentOfUniversity(Long universityId) {
        return studentRepository.getAllStudentOfUniversity(universityId);
    }

    @Override
    public List<Student> searchStudents(Long departmentId, String className, String studentCode, String name) {
        return studentRepository.searchStudents(departmentId,className,studentCode, name);
    }

    public Student findById(Long id){
        return studentRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Khong tim thay id"));
    }
}
