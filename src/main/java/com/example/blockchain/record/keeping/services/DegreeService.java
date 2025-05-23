package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.CertificateRepository;
import com.example.blockchain.record.keeping.repositorys.DegreeRepository;
import com.example.blockchain.record.keeping.repositorys.DegreeTitleRepository;
import com.example.blockchain.record.keeping.repositorys.StudentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DegreeService implements IDegreeService{

    private final CertificateRepository certificateRepository;
    private final StudentRepository studentRepository;
    private final BrevoApiEmailService brevoApiEmailService;
    private final UniversityCertificateTypeService universityCertificateTypeService;
    private final UserService userService;
    private final CertificateTypeService certificateTypeService;
    private final DegreeRepository degreeRepository;
    private final EducationModelSevice educationModelSevice;
    private final DegreeTitleSevice degreeTitleSevice;
    private final RatingService ratingService;
    private final StudentClassService studentClassService;



    @Transactional
    public void createDegree(JsonNode jsonNode,Long idClass, MultipartFile image) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        JsonNode studentNode = jsonNode.get("student");
        JsonNode degreeNode = jsonNode.get("degree");

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
                    StudentClass studentClass = studentClassService.findById(idClass);
                    newStudent.setStudentClass(studentClass);//này đổi
                    newStudent.setName(studentNode.get("name").asText());
                    newStudent.setStudentCode(studentCode);
                    newStudent.setEmail(studentNode.get("email").asText());
                    newStudent.setBirthDate(LocalDate.parse(studentNode.get("birth_date").asText()));
                    newStudent.setCourse(studentNode.get("course").asText());
                    newStudent.setCreatedAt(vietnamTime.toLocalDateTime());
                    newStudent.setUpdatedAt(vietnamTime.toLocalDateTime());
                    return studentRepository.save(newStudent); // lưu nếu chưa có
                });

        Degree degree = new Degree();
        degree.setStudent(student);

        //lấy thong tin loai chung chi
        Long id = Long.valueOf(degreeNode.get("name_certificate_type").asText());
        CertificateType certificateType= certificateTypeService.findById(id);
        UniversityCertificateType universityCertificateType = universityCertificateTypeService.findByCartificateType(certificateType);
        Rating rating = ratingService.findByName(degreeNode.get("rating").asText());
        degree.setRating(rating);
        EducationMode educationMode = educationModelSevice.findByName(degreeNode.get("education_mode").asText());
        degree.setEducationMode(educationMode);
        DegreeTitle degreeTitle = degreeTitleSevice.findByName(degreeNode.get("degree_title").asText());
        degree.setDegreeTitle(degreeTitle);
        
        degree.setIssueDate(LocalDate.parse(degreeNode.get("issue_date").asText()));
        degree.setGraduationYear(degreeNode.get("graduation_year").asText());
        degree.setTrainingLocation(degreeNode.get("training_location").asText());
        degree.setSigner(degreeNode.get("signer").asText());
        degree.setDiplomaNumber(degreeNode.get("diploma_number").asText());
        degree.setLotteryNumber(degreeNode.get("lottery_number").asText());

        //sửa lại nếu có id block
        degree.setBlockchainTxHash("631273817");

        degree.setStatus("1");
        degree.setCreatedAt(vietnamTime.toLocalDateTime());

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
        degreeRepository.save(degree);

        //sửa lại đường dẫn chứng chỉ
        String certificateUrl = "https://yourdomain.com/certificates/" + student.getStudentCode();
        try {
//            brevoApiEmailService.sendEmail(student.getEmail(), student.getName(), certificateUrl);
        } catch (Exception e) {
            System.err.println("Lỗi khi gửi email cho sinh viên: " + student.getEmail());
            e.printStackTrace(); // Hoặc ghi log nếu bạn có hệ thống logging
        }
    }


    @Override
    public Degree save(Degree degree) {
        return degreeRepository.save(degree);
    }

    @Override
    public List<Degree> listDegreeOfStudent(Student student) {
        return degreeRepository.findByStudent(student);
    }
}

