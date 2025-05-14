package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.CertificateDTO;
import com.example.blockchain.record.keeping.models.Certificate;
import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.Student;
import com.example.blockchain.record.keeping.repositorys.CertificateRepository;
import com.example.blockchain.record.keeping.repositorys.StudentRepository;
import com.example.blockchain.record.keeping.response.StudentWithCertificateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    public List<Student> listUserOfDepartment(Department department) {
        return studentRepository.findByDepartment(department);
    }

    public List<StudentWithCertificateResponse> getStudentsWithCertificatesByUniversity(Long universityId) {
        List<Student> studentList = studentRepository.findByUniversityId(universityId);
        List<StudentWithCertificateResponse> responseList = new ArrayList<>();

        for (Student student : studentList) {
            List<Certificate> certificateList = certificateRepository.findByStudentId(student.getId());

            List<CertificateDTO> certificateDTOs = certificateList.stream()
                    .map(c -> new CertificateDTO(
                            c.getIssueDate(),
                            c.getGraduationYear(),
                            c.getEducationMode(),
                            c.getTrainingLocation(),
                            c.getSigner(),
                            c.getDiplomaNumber(),
                            c.getLotteryNumber(),
                            c.getBlockchainTxHash(),
                            c.getRating(),
                            c.getDegreeTitle(),
                            c.getImageUrl(),
                            c.getStatus()
                    ))
                    .collect(Collectors.toList());

            responseList.add(new StudentWithCertificateResponse(
                    student.getDepartment().getUniversity().getName(),
                    student.getDepartment().getName(),
                    student.getName(),
                    student.getStudentCode(),
                    student.getEmail(),
                    student.getClassName(),
                    student.getBirthDate(),
                    student.getCourse(),
                    certificateDTOs
            ));
        }
        return responseList;
    }

    @Override
    public Optional<Student> findByStudentCodeAndDepartment_Id(String mssv, Long departmentId) {
        return studentRepository.findByStudentCodeAndDepartment_Id(mssv, departmentId);
    }


}
