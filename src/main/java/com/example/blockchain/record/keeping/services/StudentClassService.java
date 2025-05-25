package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.StudentClass;
import com.example.blockchain.record.keeping.repositorys.StudentClassRepository;
import com.example.blockchain.record.keeping.response.DepartmentWithClassReponse;
import com.example.blockchain.record.keeping.response.StudentClassReponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
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
    public List<StudentClass> findAllClassesByDepartmentId(Long id, String name) {
        return studentClassRepository.findAllClassesByDepartmentId(id, name);
    }

    @Override
    public List<Department> findAllDeparmentOfUniversity(Long id) {
        return studentClassRepository.findAllDeparmentOfUniversity(id);
    }

    @Override
    public boolean existsByNameAndDepartmentIdAndStatus(String name, Department department) {
        return studentClassRepository.existsByNameAndDepartmentAndStatus(name,department,Status.ACTIVE);
    }

    @Override
    public StudentClass save(StudentClass studentClass) {
        return studentClassRepository.save(studentClass);
    }

    @Override
    public StudentClass deleteStudentClass(Long id) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        StudentClass studentClass = findById(id);
        studentClass.setStatus(Status.DELETED);
        studentClass.setUpdatedAt(vietnamTime.toLocalDateTime());
        return studentClassRepository.save(studentClass);
    }

    @Override
    public List<StudentClass> searchNameClass(String name) {
        return studentClassRepository.findByNameContainingAndStatus(name, Status.ACTIVE);
    }


    public List<StudentClassReponse> getAllClassofUniversity(Long id,String name){
        List<StudentClass> studentClassList = studentClassRepository.findAllClassOfUniversityByName(id, name);
        List<StudentClassReponse> studentClassReponseList= new ArrayList<>();
        for (StudentClass studentClass : studentClassList){
            StudentClassReponse studentClassReponse = new StudentClassReponse(
                    studentClass.getId(),
                    studentClass.getName()
            );
            studentClassReponseList.add(studentClassReponse);
        }
        return  studentClassReponseList;
    }

}
