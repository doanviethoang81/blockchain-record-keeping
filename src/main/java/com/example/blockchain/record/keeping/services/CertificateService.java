package com.example.blockchain.record.keeping.services;


import com.example.blockchain.record.keeping.blockchain.CertificateStorage;
import com.example.blockchain.record.keeping.dtos.CertificateDTO;
import com.example.blockchain.record.keeping.dtos.StudentDTO;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.CertificateRepository;
import com.example.blockchain.record.keeping.repositorys.StudentRepository;
import com.example.blockchain.record.keeping.repositorys.UniversityCertificateTypeRepository;
import com.example.blockchain.record.keeping.repositorys.UniversityRepository;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.etsi.uri.x01903.v13.CertificateValuesType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import javax.swing.plaf.multi.MultiInternalFrameUI;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CertificateService implements ICertificateService{

    private final CertificateRepository certificateRepository;
    private final StudentRepository studentRepository;
    private final BrevoApiEmailService brevoApiEmailService;
    private final UniversityService universityService;
    private final UniversityCertificateTypeService universityCertificateTypeService;
    private final UserService userService;
    private final CertificateTypeService certificateTypeService;

    @Autowired
    private Web3j web3j;

    @Override
    public Certificate saveAll(CertificateDTO certificateDTO, StudentDTO studentDTO){
//        Student student = modelMapper.map(studentDTO, Student.class);
//        studentRepository.save(student);
//
//        Certificate certificate = modelMapper.map(certificateDTO, Certificate.class);
//        certificate.setStudent(student);

        return null;
    }

    @Override
    public List<Certificate> listCertificateOfStudent(Student student) {
        return certificateRepository.findByStudent(student);
    }

    @Transactional
    public void createCertificate(JsonNode jsonNode, MultipartFile image) {
        // Lấy ra node student và certificate
        JsonNode studentNode = jsonNode.get("student");
        JsonNode certificateNode = jsonNode.get("certificate");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user= userService.findByUser(username);
        Department department=user.getDepartment();

//        Long idKhoa =
        String studentCode = studentNode.get("student_code").asText();

        //kiem tra trung mssv
        Student student = studentRepository.findByStudentCode(studentCode)
                .orElseGet(() -> {
                    Student newStudent = new Student();
                    newStudent.setDepartment(department);//này đổi
                    newStudent.setName(studentNode.get("name").asText());
                    newStudent.setStudentCode(studentCode);
                    newStudent.setEmail(studentNode.get("email").asText());
                    newStudent.setClassName(studentNode.get("class_name").asText());
                    newStudent.setBirthDate(LocalDate.parse(studentNode.get("birth_date").asText()));
                    newStudent.setCourse(studentNode.get("course").asText());
                    return studentRepository.save(newStudent); // lưu nếu chưa có
                });

        Certificate certificate = new Certificate();
        certificate.setStudent(student);

        //lấy thong tin loai chung chi
        Long id = Long.valueOf(certificateNode.get("name_certificate_type").asText());
        CertificateType certificateType= certificateTypeService.findById(id);
        UniversityCertificateType universityCertificateType = universityCertificateTypeService.findByCartificateType(certificateType);

        certificate.setUniversityCertificateType(universityCertificateType);
        certificate.setIssueDate(LocalDate.parse(certificateNode.get("issue_date").asText()));
        certificate.setGraduationYear(certificateNode.get("graduation_year").asText());
        certificate.setEducationMode(certificateNode.get("education_mode").asText());
        certificate.setTrainingLocation(certificateNode.get("training_location").asText());
        certificate.setSigner(certificateNode.get("signer").asText());
        certificate.setDiplomaNumber(certificateNode.get("diploma_number").asText());
        certificate.setLotteryNumber(certificateNode.get("lottery_number").asText());

        //sửa lại nếu có id block
        certificate.setBlockchainTxHash("631273817");

        certificate.setStatus("1");
        certificate.setRating(certificateNode.get("rating").asText());
        certificate.setDegreeTitle(certificateNode.get("degree_title").asText());

        // Lưu dữ liệu lên blockchain
//            String contractAddress = "0x3D9433e04432406335CBEf89cB39784fC1Fd7d7B"; // Thay bằng địa chỉ hợp đồng của bạn
//            String privateKey = "2177b99edb64e06030c7ef418d69e95f397b7cb4640b2108539057ab2fd4e599"; // Thay bằng private key từ MetaMask
//            Credentials credentials = Credentials.create(privateKey);
//
//            CertificateStorage contract = new CertificateStorage(contractAddress, web3j, credentials);
//            TransactionReceipt receipt = contract.storeCertificate(
//                    studentCode,
//                    certificate.getDiplomaNumber(),
//                    certificate.getDegreeTitle()
//            );
//            certificate.setBlockchainTxHash(receipt.getTransactionHash());
//
//            // Lưu transaction hash vào certificate
//            certificate.setBlockchainTxHash(receipt.getTransactionHash());

        // Lưu certificate
        certificateRepository.save(certificate);

        //sửa lại đường dẫn chứng chỉ
        String certificateUrl = "https://yourdomain.com/certificates/" + student.getStudentCode();
        try {
            brevoApiEmailService.sendEmail(student.getEmail(), student.getName(), certificateUrl);
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email cho sinh viên: " + student.getEmail());
            e.printStackTrace(); // Hoặc ghi log nếu bạn có hệ thống logging
        }
    }
}
