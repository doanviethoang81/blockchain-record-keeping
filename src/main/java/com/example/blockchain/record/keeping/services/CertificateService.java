package com.example.blockchain.record.keeping.services;


import com.example.blockchain.record.keeping.dtos.CertificateDTO;
import com.example.blockchain.record.keeping.dtos.StudentDTO;
import com.example.blockchain.record.keeping.dtos.request.CertificatePrintData;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.*;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CertificateService implements ICertificateService{

    private final CertificateRepository certificateRepository;
    private final UniversityCertificateTypeService universityCertificateTypeService;
    private final CertificateTypeService certificateTypeService;
    private final GraphicsTextWriter graphicsTextWriter;

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

    //ALL chung chi admin
    @Override
    public List<Certificate> findByAllCertificate(
            String universityName,
            String departmentName,
            String className,
            String studentCode,
            String studentName
    ) {
        return certificateRepository.findByAllCertificate(
                universityName,
                departmentName,
                className,
                studentCode,
                studentName
        );
    }

    @Override
    public Certificate findById(Long id) {
        return certificateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chứng chỉ id "+ id));
    }

    @Override
    public Optional<Certificate> existingStudentOfCertificate(Long studentId, Long certificateId) {
        return certificateRepository.existingStudentOfCertificate(studentId,certificateId);
    }

    @Override
    public Certificate save(Certificate certificate) {
        return certificateRepository.save(certificate);
    }

    @Override
    public List<Certificate> saveAll(List<Certificate> certificateList) {
        return certificateRepository.saveAll(certificateList);
    }

    @Transactional
    public void createCertificate(Student student,JsonNode jsonNode) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        String certificateTypeId = jsonNode.get("certificateTypeId").asText();
        LocalDate issueDate =  LocalDate.parse(jsonNode.get("issueDate").asText());
        String diplomaNumber = jsonNode.get("diplomaNumber").asText();
        String signer = jsonNode.get("signer").asText();
        String grantor = jsonNode.get("grantor").asText();


        Certificate certificate = new Certificate();
        certificate.setStudent(student);

        //lấy thong tin loai chung chi
        Long id = Long.valueOf(certificateTypeId);
        CertificateType certificateType= certificateTypeService.findById(id);
        UniversityCertificateType universityCertificateType =
                universityCertificateTypeService.findByCartificateType(certificateType);

        certificate.setUniversityCertificateType(universityCertificateType);
        certificate.setIssueDate(issueDate);
        certificate.setDiplomaNumber(diplomaNumber);
        certificate.setSigner(signer);
        certificate.setGrantor(grantor);
        //sửa lại nếu có id block
        certificate.setBlockchainTxHash("631273817");

        //tạo ảnh
        CertificatePrintData printData = new CertificatePrintData();
        printData.setUniversityName(universityCertificateType.getUniversity().getName());
        printData.setCertificateTitle("GIẤY CHỨNG NHẬN");
        printData.setStudentName(student.getName());
        printData.setDepartmentName("Khoa " + student.getStudentClass().getDepartment().getName());
        printData.setCertificateName(certificateType.getName());
        printData.setDiplomaNumber("Số: " + diplomaNumber);
        printData.setIssueDate("Ngày " + issueDate.getDayOfMonth() + " tháng " + issueDate.getMonthValue() + " năm " + issueDate.getYear());
        printData.setGrantor(grantor);
        printData.setSigner(signer);

        String image_url = graphicsTextWriter.drawCertificateText(printData);

        certificate.setImageUrl(image_url);
        certificate.setQrCodeUrl("1313");//sua

        certificate.setStatus(Status.PENDING);
        certificate.setCreatedAt(vietnamTime.toLocalDateTime());

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
//        String certificateUrl = "https://yourdomain.com/certificates/" + student.getStudentCode();
//        try {
//            //chưa gửi
//            brevoApiEmailService.sendEmail(student.getEmail(), student.getName(), certificateUrl);
//        } catch (Exception e) {
//            System.err.println("Lỗi khi gửi email cho sinh viên: " + student.getEmail());
//            e.printStackTrace(); // Hoặc ghi log nếu bạn có hệ thống logging
//        }
    }
}
