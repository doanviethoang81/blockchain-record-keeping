package com.example.blockchain.record.keeping.services;
import com.example.blockchain.record.keeping.dtos.request.CertificatePrintData;
import com.example.blockchain.record.keeping.dtos.request.CertificateRequest;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.*;
import com.example.blockchain.record.keeping.response.ApiResponseBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CertificateService implements ICertificateService{

    private final CertificateRepository certificateRepository;
    private final UniversityCertificateTypeService universityCertificateTypeService;
    private final CertificateTypeService certificateTypeService;
    private final GraphicsTextWriter graphicsTextWriter;
    private final BrevoApiEmailService brevoApiEmailService;

    @Autowired
    private Web3j web3j;

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

    @Override
    public Certificate findByIdAndStatus(Long id, Status status) {
        return certificateRepository.findByIdAndStatus(id,status);
    }

    @Override
    public List<Certificate> listCertificateOfDepartment(Long departmentId, String className, String studentCode, String studentName) {
        return certificateRepository.listCertificateOfDepartment(departmentId, className,studentCode,studentName );
    }

    @Override
    public List<Certificate> listCertificateOfDepartmentPending(Long departmentId, String className, String studentCode, String studentName) {
        return certificateRepository.listCertificateOfDepartmentPending(departmentId, className,studentCode,studentName );
    }

    @Override
    public List<Certificate> listCertificateOfUniversity(Long universittyId, String departmentName, String className, String studentCode, String studentName) {
        return certificateRepository.listCertificateOfUniversity(universittyId,departmentName,className,studentCode,studentName);
    }

    @Override
    public Certificate update(Certificate certificate, CertificateRequest request) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate issueDate = LocalDate.parse(request.getIssueDate(), formatter);

        certificate.setIssueDate(issueDate);
        certificate.setDiplomaNumber(request.getDiplomaNumber());
        certificate.setGrantor(request.getGrantor());
        certificate.setSigner(request.getSigner());
        certificate.setUpdatedAt(vietnamTime.toLocalDateTime());

        // tạo anh
        CertificatePrintData printData = new CertificatePrintData();
        printData.setUniversityName(certificate.getUniversityCertificateType().getUniversity().getName());
        printData.setCertificateTitle("GIẤY CHỨNG NHẬN");
        printData.setStudentName(certificate.getStudent().getName());
        printData.setDepartmentName("Khoa " + certificate.getStudent().getStudentClass().getDepartment().getName());
        printData.setCertificateName(certificate.getUniversityCertificateType().getCertificateType().getName());
        printData.setDiplomaNumber("Số: " + request.getDiplomaNumber());
        printData.setIssueDate("Ngày " + issueDate.getDayOfMonth() + " tháng " + issueDate.getMonthValue() + " năm " + issueDate.getYear());
        printData.setGrantor(request.getGrantor());
        printData.setSigner(request.getSigner());

        String image = graphicsTextWriter.drawCertificateText(printData);
        certificate.setImageUrl(image);
        return certificateRepository.save(certificate);
    }

    @Override
    public List<Certificate> listCertificateOfUniversityPending(Long universittyId, String departmentName, String className, String studentCode, String studentName) {
        return certificateRepository.listCertificateOfUniversityPending(universittyId,departmentName,className,studentCode,studentName);
    }

    @Transactional
    public void createCertificate(Student student,JsonNode jsonNode) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String certificateTypeId = jsonNode.get("certificateTypeId").asText();
//        LocalDate issueDate =  LocalDate.parse();
        String diplomaNumber = jsonNode.get("diplomaNumber").asText();
        String signer = jsonNode.get("signer").asText();
        String grantor = jsonNode.get("grantor").asText();

        LocalDate issueDate = LocalDate.parse(jsonNode.get("issueDate").asText(), formatter);


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
        certificate.setUpdatedAt(vietnamTime.toLocalDateTime());
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

    // them dau moc
    @Transactional
    public void certificateValidation (University university,Long idCertificate) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        Certificate certificate = certificateRepository.findById(idCertificate)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy chứng chỉ có id "+ idCertificate));

        String image_url = graphicsTextWriter.certificateValidation(certificate.getImageUrl(), university.getLogo());

        certificate.setImageUrl(image_url);
        certificate.setStatus(Status.APPROVED);
        certificate.setUpdatedAt(vietnamTime.toLocalDateTime());
        certificateRepository.save(certificate);

//        //sửa lại đường dẫn chứng chỉ
//        String certificateUrl = "https://yourdomain.com/certificates/" + certificate.getStudent().getStudentCode();
//        try {
//            //chưa gửi
//            brevoApiEmailService.sendEmail(certificate.getStudent().getEmail(), certificate.getStudent().getName(), certificateUrl);
//        } catch (Exception e) {
//            System.err.println("Lỗi khi gửi email cho sinh viên: " + certificate.getStudent().getEmail());
//            e.printStackTrace(); // Hoặc ghi log nếu bạn có hệ thống logging
//        }
    }
}
