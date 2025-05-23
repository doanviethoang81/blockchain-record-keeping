package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.CertificateDTO;
import com.example.blockchain.record.keeping.dtos.DegreeDTO;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.CertificateRepository;
import com.example.blockchain.record.keeping.repositorys.StudentRepository;
import com.example.blockchain.record.keeping.response.ClassWithStudentsResponse;
import com.example.blockchain.record.keeping.response.DepartmentWithClassWithStudentResponse;
import com.example.blockchain.record.keeping.response.StudentDetailReponse;
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
    public List<ClassWithStudentsResponse> studentOfClassOfDepartmentList(Long idDepartment){
        List<StudentClass> studentClassList = studentClassService.findAllClassesByDepartmentId(idDepartment);
        List<ClassWithStudentsResponse> classWithStudentsResponseList = new ArrayList<>();

        for (StudentClass studentClass : studentClassList) {
            List<Student> studentList = studentRepository.findByStudentClassId(studentClass.getId());

            List<StudentDetailReponse> studentDetailReponseList = new ArrayList<>();
            for (Student student : studentList) {
                //1 list chứng chỉ
                List<Degree> degreeList = degreeService.listDegreeOfStudent(student);

                List<Certificate> certificateList = certificateService.listCertificateOfStudent(student);

                List<DegreeDTO> degreeDTOList = degreeList.stream()
                        .map(u -> new DegreeDTO(
                                u.getIssueDate(),
                                u.getGraduationYear(),
                                u.getEducationMode().getName(),
                                u.getTrainingLocation(),
                                u.getSigner(),
                                u.getDiplomaNumber(),
                                u.getLotteryNumber(),
                                u.getBlockchainTxHash(),
                                u.getRating().getName(),
                                u.getDegreeTitle().getName(),
                                u.getImageUrl(),
                                u.getCreatedAt()
                        ))
                        .collect(Collectors.toList());

                List<CertificateDTO> certificateDTOList = certificateList.stream()
                        .map(u -> new CertificateDTO(
                                u.getUniversityCertificateType().getCertificateType().getName(),
                                u.getIssueDate(),
                                u.getDiplomaNumber(),
                                u.getBlockchainTxHash(),
                                u.getImageUrl(),
                                u.getQrCodeUrl(),
                                u.getCreatedAt()
                        ))
                        .collect(Collectors.toList());

                StudentDetailReponse studentDetailReponse = new StudentDetailReponse(
                        student.getName(),
                        student.getStudentCode(),
                        student.getEmail(),
                        student.getBirthDate(),
                        student.getCourse(),
                        degreeDTOList,
                        certificateDTOList
                );
                studentDetailReponseList.add(studentDetailReponse);
            }
            ClassWithStudentsResponse classWithStudentsResponse = new ClassWithStudentsResponse(
                    studentClass.getName(),
                    studentDetailReponseList
            );
            classWithStudentsResponseList.add(classWithStudentsResponse);
        }
        return  classWithStudentsResponseList;
    }

    // danh sách sv của các lớp của 1 khoa của 1 trường
    public List<DepartmentWithClassWithStudentResponse> getStudentsWithCertificatesByUniversity(University university) {

        List<DepartmentWithClassWithStudentResponse> departmentWithClassWithStudentResponseList = new ArrayList<>();
        List<Department> departmentList = departmentService.listDepartmentOfUniversity(university);

        for (Department department : departmentList){

            DepartmentWithClassWithStudentResponse departmentWithClassWithStudentResponse
                    = new DepartmentWithClassWithStudentResponse(
                            department.getName(),
                        studentOfClassOfDepartmentList(department.getId()) // gọi tk trên để lấy ra danh sách lớp có sinh viên
            );
            departmentWithClassWithStudentResponseList.add(departmentWithClassWithStudentResponse);
        }
        return departmentWithClassWithStudentResponseList;
    }

    @Override
    public Optional<Student> findByStudentCodeAndDepartmentId(String studentCode, Long departmentId) {
        return studentRepository.findByStudentCodeAndDepartmentId(studentCode,departmentId);
    }

    public Student findById(Long id){
        return studentRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Khong tim thay id"));
    }
}
