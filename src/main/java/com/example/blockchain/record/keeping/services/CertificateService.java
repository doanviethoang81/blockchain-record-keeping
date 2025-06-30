package com.example.blockchain.record.keeping.services;
import com.example.blockchain.record.keeping.configs.Constants;
import com.example.blockchain.record.keeping.dtos.request.*;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.*;
import com.example.blockchain.record.keeping.response.CertificateOfStudentResponse;
import com.example.blockchain.record.keeping.response.CertificateResponse;
import com.example.blockchain.record.keeping.response.CountCertificateTypeResponse;
import com.example.blockchain.record.keeping.response.MonthlyCertificateStatisticsResponse;
import com.example.blockchain.record.keeping.utils.PinataUploader;
import com.example.blockchain.record.keeping.utils.QrCodeUtil;
import com.example.blockchain.record.keeping.utils.RSAUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import java.security.PrivateKey;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
            String studentName,
            String diplomaNumber
    ) {
        return certificateRepository.findByAllCertificate(
                universityName,
                departmentName,
                className,
                studentCode,
                studentName,
                diplomaNumber
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
    public List<Certificate> listCertificateOfDepartment(Long departmentId, String className, String studentCode, String studentName, String diplomaNumber, int limit, int offset) {
        return certificateRepository.listCertificateOfDepartment(departmentId, className,studentCode,studentName ,diplomaNumber, limit,offset);
    }

    @Override
    public List<Certificate> listCertificateOfDepartmentAndStatus(Long departmentId, String className, String studentCode, String studentName,String diplomaNumber, String status, int limit, int offset) {
        return certificateRepository.listCertificateOfDepartmentAndStatus(departmentId, className,studentCode,studentName,diplomaNumber,status, limit,offset);
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
    public List<MonthlyCertificateStatisticsResponse> monthlyCertificateStatisticsOfUniversity(Long universityId) {
        List<Object[]> raw =  certificateRepository.monthlyCertificateStatisticsOfUniversity(universityId);

        return raw.stream().map(row -> new MonthlyCertificateStatisticsResponse(
                ((Number) row[0]).intValue(),
                ((Number) row[1]).longValue(),
                ((Number) row[2]).longValue(),
                ((Number) row[3]).longValue()
        )).collect(Collectors.toList());
    }

    @Override
    public List<MonthlyCertificateStatisticsResponse> monthlyCertificateStatisticsOfDepartment(Long departmentId) {
        List<Object[]> raw =  certificateRepository.monthlyCertificateStatisticsOfDepartment(departmentId);

        return raw.stream().map(row -> new MonthlyCertificateStatisticsResponse(
                ((Number) row[0]).intValue(),
                ((Number) row[1]).longValue(),
                ((Number) row[2]).longValue(),
                ((Number) row[3]).longValue()
        )).collect(Collectors.toList());
    }

    @Override
    public List<CountCertificateTypeResponse> countCertificateTypeOfUniversity(Long universityId) {
        List<CountCertificateTypeRequest> count = certificateRepository.countCertificateTypeOfUniversity(universityId);
        List<CountCertificateTypeResponse> responseList = new ArrayList<>();
        for (CountCertificateTypeRequest request : count){
            CountCertificateTypeResponse response = new CountCertificateTypeResponse(
                    request.getName(),
                    request.getApproved(),
                    request.getPercentage()
            );
            responseList.add(response);
        }
        return responseList;
    }

    @Override
    public List<CountCertificateTypeResponse> countCertificateTypeOfDepartment(Long departmentId) {
        List<CountCertificateTypeRequest> count = certificateRepository.countCertificateTypeOfDepartment(departmentId);
        List<CountCertificateTypeResponse> responseList = new ArrayList<>();
        for (CountCertificateTypeRequest request : count){
            CountCertificateTypeResponse response = new CountCertificateTypeResponse(
                    request.getName(),
                    request.getApproved(),
                    request.getPercentage()
            );
            responseList.add(response);
        }
        return responseList;
    }

    @Override
    public long countCertificatesOfUniversity(Long universittyId, String departmentName, String className, String studentCode, String studentName, String diplomaNumber) {
        return certificateRepository.countCertificatesOfUniversity(universittyId,departmentName,className,studentCode,studentName, diplomaNumber);
    }

    @Override
    public long countCertificatesOfUniversityAndStatus(Long universittyId, String departmentName, String className, String studentCode, String studentName, String diplomaNumber, String status) {
        return certificateRepository.countCertificatesOfUniversityAndStatus(universittyId,departmentName,className,studentCode,studentName, diplomaNumber, status);
    }

    @Override
    public long countCertificatesOfDepartment(Long departmentId,String className, String studentCode, String studentName, String diplomaNumber) {
        return certificateRepository.countCertificatesOfDepartment(departmentId,className,studentCode,studentName, diplomaNumber);
    }

    @Override
    public long countCertificatesOfDepartmentOfStatus(Long departmentId, String className, String studentCode, String studentName, String diplomaNumber, String status) {
        return certificateRepository.countCertificatesOfDepartmentAndStatus(departmentId,className,studentCode,studentName, diplomaNumber, status);
    }

    @Override
    public List<CertificateOfStudentResponse> certificateOfStudent(Long studentId, String diplomaNumber, int limit, int offset) {
        List<CertificateOfStudentResponse> result = new ArrayList<>();
        List<Certificate> certificateList = certificateRepository.certificateOfStudent(studentId,diplomaNumber,limit,offset);
        for(Certificate certificate : certificateList){
            CertificateOfStudentResponse response = new CertificateOfStudentResponse(
                    certificate.getId(),
                    certificate.getStudent().getName(),
                    certificate.getStudent().getStudentClass().getName(),
                    certificate.getStudent().getStudentClass().getDepartment().getName(),
                    certificate.getIssueDate(),
                    convertStatusToDisplay(certificate.getStatus()),
                    certificate.getDiplomaNumber(),
                    certificate.getDiplomaNumber(),
                    certificate.getCreatedAt()
            );
            result.add(response);
        }
        return result;
    }

    @Override
    public long countCertificateOfStudent(Long studentId, String diplomaNumber) {
        return certificateRepository.countCertificateOfStudent(studentId,diplomaNumber);
    }

    @Override
    public List<Certificate> listCertificateOfUniversity(Long universittyId, String departmentName, String className, String studentCode, String studentName, String diplomaNumber, int limit, int offset){
        return certificateRepository.findPagedCertificates(universittyId,departmentName,className,studentCode,studentName, diplomaNumber, limit, offset);
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
    public List<Certificate> listCertificateOfUniversityAndStatus(Long universittyId, String departmentName, String className, String studentCode, String studentName,String diplomaNumber, String status, int limit, int offset){
        return certificateRepository.listCertificateOfUniversityAndStatus(universittyId,departmentName,className,studentCode,studentName,diplomaNumber, status, limit,offset);
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // từ chối xác nhận chứng chỉ
    @Transactional
    public void certificateRejected (University university,Long idCertificate) throws Exception {
        try {
            ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            Certificate certificate = certificateRepository.findById(idCertificate)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chứng chỉ có id " + idCertificate));

            certificate.setStatus(Status.REJECTED);
            certificate.setUpdatedAt(vietnamTime.toLocalDateTime());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public Set<String> findAllDiplomaNumbers(Collection<String> diplomaNumbers) {
        return new HashSet<>(certificateRepository.findExistingDiplomaNumbers(diplomaNumbers));
    }

    private String convertStatusToDisplay(Status status) {
        return switch (status) {
            case PENDING -> "Chưa duyệt";
            case APPROVED -> "Đã duyệt";
            case REJECTED -> "Đã từ chối";
            default -> "Không xác định";
        };
    }
}
