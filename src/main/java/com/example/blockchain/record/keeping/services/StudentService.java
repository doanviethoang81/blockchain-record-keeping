package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.request.StudentRequest;
import com.example.blockchain.record.keeping.dtos.request.UpdateStudentRequest;
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
import java.util.Set;

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
    public Student findByStudentCodeOfUniversity(String studentCode, Long universityId) {
        return studentRepository.findByStudentCodeOfUniversity(studentCode,universityId).orElse(null);
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

    @Override
    public Student update(Long id , UpdateStudentRequest updateStudentRequest) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        Student student = studentRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy sinh viên với id "+ id));
        StudentClass studentClass = studentClassRepository.findById(updateStudentRequest.getClassId())
                .orElseThrow(()-> new RuntimeException("Không tìm lớp với id "+ updateStudentRequest.getClassId()));
        student.setStudentClass(studentClass);
        student.setName(updateStudentRequest.getName());
        student.setStudentCode(updateStudentRequest.getStudentCode());
        student.setEmail(updateStudentRequest.getEmail());
        student.setBirthDate(updateStudentRequest.getBirthDate());
        student.setCourse(updateStudentRequest.getCourse());
        student.setUpdatedAt(vietnamTime.toLocalDateTime());
        return studentRepository.save(student);
    }

    @Override
    public Student delete(Long id) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        Student student = studentRepository.findByIdAndStatus(id, Status.ACTIVE )
                .orElseThrow(()-> new RuntimeException("Không tìm thấy sinh viên với id "+ id));
        student.setStatus(Status.DELETED);
        student.setUpdatedAt(vietnamTime.toLocalDateTime());
        return studentRepository.save(student);
    }

    @Override
    public Optional<StudentClass> findByClassNameAndDepartmentId(Long departmentId, String className) {
        return studentClassRepository.findByClassNameAndDepartmentId(departmentId,className);
    }

    @Override
    public List<Student> findByStudentOfDepartment(Long departmentId, String studentCode) {
        return studentRepository.findByStudentOfDepartment(departmentId,studentCode);
    }

    @Override
    public Optional<Student> findByOneStudentOfDepartment(Long departmentId, String studentCode) {
        return studentRepository.findByOneStudentOfDepartment(departmentId,studentCode);
    }

    @Override
    public List<Student> findByStudentCodesOfDepartment(Long departmentId, Set<String> allStudentCodes) {
        return studentRepository.findByStudentCodesOfDepartment(departmentId,allStudentCodes);
    }

    @Override
    public Student findByEmail(String email) {
        return studentRepository.findByEmail(email)
                .orElse(null);
    }

    public Student findById(Long id){
        return studentRepository.findByIdAndStatus(id, Status.ACTIVE).orElse(null);
    }
}
