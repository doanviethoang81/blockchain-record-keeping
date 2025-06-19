package com.example.blockchain.record.keeping.services;
import com.certificate.contract.CertificateStorage_sol_EncryptedCertificateStorage;
import com.example.blockchain.record.keeping.configs.Constants;
import com.example.blockchain.record.keeping.dtos.request.CertificateBlockchainRequest;
import com.example.blockchain.record.keeping.dtos.request.CertificatePrintData;
import com.example.blockchain.record.keeping.dtos.request.CertificateRequest;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.*;
import com.example.blockchain.record.keeping.utils.EnvUtil;
import com.example.blockchain.record.keeping.utils.PinataUploader;
import com.example.blockchain.record.keeping.utils.QrCodeUtil;
import com.example.blockchain.record.keeping.utils.RSAUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.utils.Numeric;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CertificateService implements ICertificateService{

    private final CertificateRepository certificateRepository;
    private final UniversityCertificateTypeService universityCertificateTypeService;
    private final CertificateTypeService certificateTypeService;
    private final GraphicsTextWriter graphicsTextWriter;
    private final BrevoApiEmailService brevoApiEmailService;
    private final QrCodeUtil qrCodeUtil;
    private final StudentRepository studentRepository;
    private final BlockChainService blockChainService;

    @Autowired
    private Web3j web3j;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RSAUtil rsaUtil;

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
    public Certificate findByIpfsUrl(String ipfsUrl) {
        return certificateRepository.findByIpfsUrl(ipfsUrl);
    }

    @Override
    public Map<String, Boolean> findCertificatesOfStudentsByType(Set<Long> studentIds, Long certificateTypeId) {
        List<String> studentCodesWithCert = certificateRepository.findStudentCodesWithCertificateNative(studentIds, certificateTypeId);
        Map<String, Boolean> result = new HashMap<>();

        for (Long id : studentIds) {
            String studentCode = studentRepository.findByStudentCode(id);
            result.put(studentCode, studentCodesWithCert.contains(studentCode));
        }
        return result;
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
        String diplomaNumber = jsonNode.get("diplomaNumber").asText();
        String signer = jsonNode.get("signer").asText();
        String grantor = jsonNode.get("grantor").asText();

        LocalDate issueDate = LocalDate.parse(jsonNode.get("issueDate").asText(), formatter);


        Certificate certificate = new Certificate();
        certificate.setStudent(student);

        Long id = Long.valueOf(certificateTypeId);
        CertificateType certificateType= certificateTypeService.findById(id);
        UniversityCertificateType universityCertificateType =
                universityCertificateTypeService.findByCartificateType(certificateType);

        certificate.setUniversityCertificateType(universityCertificateType);
        certificate.setIssueDate(issueDate);
        certificate.setDiplomaNumber(diplomaNumber);
        certificate.setSigner(signer);
        certificate.setGrantor(grantor);
        certificate.setBlockchainTxHash(null);

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
        certificate.setQrCodeUrl(null);//sua

        certificate.setStatus(Status.PENDING);
        certificate.setCreatedAt(vietnamTime.toLocalDateTime());
        certificate.setUpdatedAt(vietnamTime.toLocalDateTime());
        // Lưu certificate
        certificateRepository.save(certificate);
    }

    // them dau moc ch ch
    @Transactional
    public void certificateValidation (University university,Long idCertificate) throws Exception {
        try {
            ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            Certificate certificate = certificateRepository.findById(idCertificate)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chứng chỉ có id " + idCertificate));

            String imageUrl = graphicsTextWriter.certificateValidation(certificate.getImageUrl(), university.getSealImageUrl());
            certificate.setImageUrl(imageUrl);

            String ipfsUrl = PinataUploader.uploadFromUrlToPinata(imageUrl);
            certificate.setIpfsUrl(ipfsUrl);

            //đường dẫn chứng chỉ IPFS
            String certificateUrl = Constants.VERIFY_URL + ipfsUrl +"&type=certificate";

            String qrBase64 = qrCodeUtil.generateQRCodeBase64(certificateUrl, 250, 250);
            certificate.setQrCodeUrl(qrBase64);

            certificate.setStatus(Status.APPROVED);
            certificate.setUpdatedAt(vietnamTime.toLocalDateTime());
            brevoApiEmailService.sendEmailsToStudentsExcel(
                    certificate.getStudent().getEmail(),
                    certificate.getStudent().getName(),
                    university.getName(),
                    certificateUrl,
                    "Chứng chỉ");

            CertificateBlockchainRequest request = new CertificateBlockchainRequest(
                    certificate.getStudent().getName(),
                    university.getName(),
                    certificate.getIssueDate().format(formatter),
                    certificate.getDiplomaNumber(),
                    ipfsUrl
            );
            String base64PrivateKey = university.getPrivateKey();
            PrivateKey privateKey = RSAKeyPairGenerator.getPrivateKeyFromBase64(base64PrivateKey);
            String json = objectMapper.writeValueAsString(request);
            String encryptedHex = rsaUtil.encryptWithPrivateKeyToHex(json, privateKey);

            //gửi blockchain và lấy txHash naof goij thi mo
            String txHash = blockChainService.issue(encryptedHex);

//            String txHash = "123"; //sua
            certificate.setBlockchainTxHash(txHash);
//            certificateRepository.save(certificate);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Set<String> findAllDiplomaNumbers(Collection<String> diplomaNumbers) {
        return new HashSet<>(certificateRepository.findExistingDiplomaNumbers(diplomaNumbers));
    }
}
