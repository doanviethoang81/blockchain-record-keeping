package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.request.StudentRequest;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.StudentClassRepository;
import com.example.blockchain.record.keeping.repositorys.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentService implements IStudentService{
    private final StudentRepository studentRepository;
    private final StudentClassRepository studentClassRepository;

    // danh sách sv của các lớp của 1 khoa
    public List<Student> studentOfClassOfDepartmentList(Long idDepartment){
        List<Student> studentList = studentRepository.getAllStudentOfDepartment(idDepartment);
         return  studentList;
    }

    // tìm mssv của 1 class
    @Override
    public Student findByStudentCodeOfClass(String studentCode, Long classId) {
        return studentRepository.findByStudentCodeOfClass(studentCode,classId).orElse(null);
    }

    @Override
    public List<Student> getAllStudentOfUniversity(Long universityId, String departmentName, String className, String studentCode, String studentName) {
        return studentRepository.searchStudentsByUniversity(universityId,departmentName,className,studentCode,studentName);
    }

    @Override
    public List<Student> searchStudents(Long departmentId, String className, String studentCode, String name) {
        return studentRepository.searchStudents(departmentId,className,studentCode, name);
    }

    @Override
    public Student createStudent(StudentRequest studentRequest) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        StudentClass studentClass = studentClassRepository.findById(studentRequest.getClassId())
                .orElseThrow(()-> new RuntimeException("Không tìm thấy lớp với id "+ studentRequest.getClassId()));
        Student student = new Student();
        student.setStudentClass(studentClass);
        student.setName(studentRequest.getName());
        student.setStudentCode(studentRequest.getStudentCode());
        student.setEmail(studentRequest.getEmail());
        student.setBirthDate(studentRequest.getBirthDate());
        student.setCourse(studentRequest.getCourse());
        student.setStatus(Status.ACTIVE);
        student.setCreatedAt(vietnamTime.toLocalDateTime());
        student.setUpdatedAt(vietnamTime.toLocalDateTime());
        return studentRepository.save(student);
    }

    @Override
    public Student findByEmailStudentCodeOfDepartment(String email, Long departmentId) {
        return studentRepository.findByEmailStudentCodeOfDepartment(email,departmentId).orElse(null);
    }

    public Student findById(Long id){
        return studentRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Khong tim thay id"));
    }
}
