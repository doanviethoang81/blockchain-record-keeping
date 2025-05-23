package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.StudentClass;
import com.example.blockchain.record.keeping.repositorys.StudentClassRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentClassService implements IStudentClassService{

    private final StudentClassRepository studentClassRepository;

    @Override
    public StudentClass findById(Long id) {
        return studentClassRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy lớp có id "+ id));
    }

    @Override
    public StudentClass findByName(String name) {
        return studentClassRepository.findByName(name)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy lớp có tên "+ name));
    }

    @Override
    public List<StudentClass> findAllClassesByDepartmentId(Long id) {
        return studentClassRepository.findAllClassesByDepartmentId(id);
    }

}
