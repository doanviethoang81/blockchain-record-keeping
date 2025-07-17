package com.example.blockchain.record.keeping.services;
import com.STUcoin.contract.STUcoin_sol_STUcoin;
import com.alibaba.excel.EasyExcel;
import com.example.blockchain.record.keeping.annotation.Auditable;
import com.example.blockchain.record.keeping.aspect.AuditingContext;
import com.example.blockchain.record.keeping.configs.Constants;
import com.example.blockchain.record.keeping.dtos.CertificateExcelDTO;
import com.example.blockchain.record.keeping.dtos.request.*;
import com.example.blockchain.record.keeping.enums.*;
import com.example.blockchain.record.keeping.exceptions.BadRequestException;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.*;
import com.example.blockchain.record.keeping.response.*;
import com.example.blockchain.record.keeping.utils.EnvUtil;
import com.example.blockchain.record.keeping.utils.PinataUploader;
import com.example.blockchain.record.keeping.utils.QrCodeUtil;
import com.example.blockchain.record.keeping.utils.RSAUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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
    private final AuditLogService auditLogService;
    private final HttpServletRequest httpServletRequest;
    private final LogRepository logRepository;
    private final ActionChangeRepository actionChangeRepository;
    private final UserService userService;
    private final NotificateService notificateService;
    private final NotificationReceiverService notificationReceiverService;
    private final WalletService walletService;
    private final STUcoinService stUcoinService;
    private final NotificationWebSocketSender notificationWebSocketSender;

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
                    certificate.getStatus().getLabel(),
                    certificate.getDiplomaNumber(),
                    certificate.getUniversityCertificateType().getCertificateType().getName(),
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
    public boolean existByDiplomaNumber(Long universityId, String diplomaNumber) {
        Certificate certificate = certificateRepository.existByDiplomaNumber(universityId,diplomaNumber);
        if(certificate == null){
            return false;
        }
        return true;
    }

    @Override
    public boolean existByDiplomaNumberIgnoreId(Long universityId, String diplomaNumber, Long certificateId) {
        Certificate certificate = certificateRepository.existByDiplomaNumberIgnoreId(universityId,diplomaNumber, certificateId);
        if(certificate == null){
            return false;
        }
        return true;
    }

    @Override
    public List<Certificate> findByStatus(String status) {
        return certificateRepository.findByStatus(status);
    }

    @Override
    public List<Certificate> listCertificateOfUniversity(Long universittyId, String departmentName, String className, String studentCode, String studentName, String diplomaNumber, int limit, int offset){
        return certificateRepository.findPagedCertificates(universittyId,departmentName,className,studentCode,studentName, diplomaNumber, limit, offset);
    }

    @Override
    public boolean update(Long certificateId, CertificateRequest request) {
        Certificate certificate = certificateRepository.findByIdAndStatus(certificateId,Status.PENDING);

        if(certificate == null){
            throw  new BadRequestException("Chứng chỉ này đã được duyệt không chỉnh sửa được!");
        }
        Certificate certificateOld = auditLogService.cloneCertificate(certificate);

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
        certificateRepository.save(certificate);
        Certificate certificateNew = certificate;

        String ipAdress = auditLogService.getClientIp(httpServletRequest);
        List<ActionChange> changes = auditLogService.compareObjects(null, certificateOld, certificateNew);
        if (!changes.isEmpty()) {// nếu khác old mới lưu
            Log log = new Log();
            log.setUser(auditLogService.getCurrentUser());
            log.setActionType(ActionType.UPDATED);
            log.setEntityName(Entity.certificates);
            log.setEntityId(certificateId);
            log.setDescription(LogTemplate.UPDATE_CERTIFICATE.format(certificate.getDiplomaNumber()));
            log.setIpAddress(ipAdress);
            log.setCreatedAt(vietnamTime.toLocalDateTime());

            log = logRepository.save(log);

            for (ActionChange change : changes) {
                change.setLog(log);
            }
            actionChangeRepository.saveAll(changes);
        }
        return true;
    }

    @Override
    public List<Certificate> listCertificateOfUniversityAndStatus(Long universittyId, String departmentName, String className, String studentCode, String studentName,String diplomaNumber, String status, int limit, int offset){
        return certificateRepository.listCertificateOfUniversityAndStatus(universittyId,departmentName,className,studentCode,studentName,diplomaNumber, status, limit,offset);
    }

    @Transactional
    public void createCertificate(Student student,JsonNode jsonNode, User user) {
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
        certificateRepository.save(certificate);

        //log
        String ipAdress = auditLogService.getClientIp(httpServletRequest);
        Log log = new Log();
        log.setUser(auditLogService.getCurrentUser());
        log.setActionType(ActionType.CREATED);
        log.setEntityName(Entity.certificates);
        log.setEntityId(certificate.getId());
        log.setDescription(LogTemplate.CREATE_CERTIFICATE.format(certificate.getDiplomaNumber()));
        log.setIpAddress(ipAdress);
        log.setCreatedAt(vietnamTime.toLocalDateTime());
        logRepository.save(log);

        Notifications notifications = new Notifications();
        notifications.setUser(user);
        notifications.setTitle(NotificationType.CERTIFICATE_CREATED.getName());
        notifications.setContent("Khoa "+ student.getStudentClass().getDepartment().getName().toLowerCase() +" đã tạo chứng chỉ có số hiệu: "+ certificate.getDiplomaNumber());
        notifications.setType(NotificationType.CERTIFICATE_CREATED);
        notifications.setDocumentType("CERTIFICATE");
        notifications.setDocumentId(certificate.getId());
        notificateService.save(notifications);

        User userUniversity = userService.findByUser(certificate.getStudent().getStudentClass().getDepartment().getUniversity().getEmail());

        NotificationReceivers notificationReceivers = new NotificationReceivers();
        notificationReceivers.setNotification(notifications);
        notificationReceivers.setReceiverId(userUniversity.getId());
        notificationReceivers.setCreatedAt(vietnamTime.toLocalDateTime());
        notificationReceiverService.save(notificationReceivers);

        //gửi WebSocket
        NotificationResponse response = new NotificationResponse(
                notificationReceivers.getId(),
                notifications.getTitle(),
                notifications.getContent(),
                notifications.getType(),
                false,
                notifications.getDocumentType(),
                notifications.getDocumentId(),
                notificationReceivers.getCreatedAt()
        );
        notificationWebSocketSender.sendNotification(userUniversity.getId(), response);
    }

    // them dau moc 1 ch ch
    @Transactional
    @Auditable(action = ActionType.VERIFIED, entity = Entity.certificates)
    public Certificate certificateValidation (University university,Long idCertificate) throws Exception {
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

            String txHash = blockChainService.issue(encryptedHex);
            certificate.setBlockchainTxHash(txHash);

//            brevoApiEmailService.sendEmailsToStudentsExcel(
//                    certificate.getStudent().getEmail(),
//                    certificate.getStudent().getName(),
//                    university.getName(),
//                    certificateUrl,
//                    "Chứng Chỉ");

            AuditingContext.setDescription("Xác thực chứng chỉ số hiệu bằng: " + certificate.getDiplomaNumber());

            User user = userService.findByUser(university.getEmail());

            Notifications notifications = new Notifications();
            notifications.setUser(user);
            notifications.setTitle(NotificationType.CERTIFICATE_APPROVED.getName());
            notifications.setContent("Phòng đào tạo đã xác nhận chứng chỉ có số hiệu: "+ certificate.getDiplomaNumber());
            notifications.setType(NotificationType.CERTIFICATE_APPROVED);
            notifications.setDocumentType("CERTIFICATE");
            notifications.setDocumentId(certificate.getId());
            notificateService.save(notifications);

            User userDepartment = userService.findByDepartment(certificate.getStudent().getStudentClass().getDepartment());

            NotificationReceivers notificationReceivers = new NotificationReceivers();
            notificationReceivers.setNotification(notifications);
            notificationReceivers.setReceiverId(userDepartment.getId());
            notificationReceivers.setCreatedAt(vietnamTime.toLocalDateTime());
            notificationReceiverService.save(notificationReceivers);

            Wallet wallet= walletService.findByStudent(certificate.getStudent());
            if (wallet == null || wallet.getWalletAddress() == null) {
                throw new RuntimeException("Không tìm thấy ví của sinh viên");
            }
            //gửi token
            stUcoinService.transferToStudent(wallet.getWalletAddress(), new BigInteger("5").multiply(BigInteger.TEN.pow(18))); // 5 STUcoin (18 decimals)

            //gửi WebSocket
            NotificationResponse response = new NotificationResponse(
                    notificationReceivers.getId(),
                    notifications.getTitle(),
                    notifications.getContent(),
                    notifications.getType(),
                    false,
                    notifications.getDocumentType(),
                    notifications.getDocumentId(),
                    notificationReceivers.getCreatedAt()
            );
            notificationWebSocketSender.sendNotification(userDepartment.getId(), response);

            return certificate;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // xác nhận 1 list ch ch
    @Transactional
    public void confirmCertificates(List<Long> ids, User user, HttpServletRequest request) throws Exception {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Long id : ids) {
            Certificate certificate = certificateRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chứng chỉ có id " + id));

            String imageUrl = graphicsTextWriter.certificateValidation(certificate.getImageUrl(), user.getUniversity().getSealImageUrl());
            certificate.setImageUrl(imageUrl);

            String ipfsUrl = PinataUploader.uploadFromUrlToPinata(imageUrl);
            certificate.setIpfsUrl(ipfsUrl);

            String certificateUrl = Constants.VERIFY_URL + ipfsUrl + "&type=certificate";
            String qrBase64 = qrCodeUtil.generateQRCodeBase64(certificateUrl, 250, 250);
            certificate.setQrCodeUrl(qrBase64);

            certificate.setStatus(Status.APPROVED);
            certificate.setUpdatedAt(vietnamTime.toLocalDateTime());

            CertificateBlockchainRequest bcRequest = new CertificateBlockchainRequest(
                    certificate.getStudent().getName(),
                    user.getUniversity().getName(),
                    certificate.getIssueDate().format(formatter),
                    certificate.getDiplomaNumber(),
                    ipfsUrl
            );
            String privateKeyBase64 = user.getUniversity().getPrivateKey();
            PrivateKey privateKey = RSAKeyPairGenerator.getPrivateKeyFromBase64(privateKeyBase64);
            String json = objectMapper.writeValueAsString(bcRequest);
            String encryptedHex = rsaUtil.encryptWithPrivateKeyToHex(json, privateKey);
            String txHash = blockChainService.issue(encryptedHex);
            certificate.setBlockchainTxHash(txHash);

            // NÀO CHAY THI MO
            brevoApiEmailService.sendEmailsToStudentsExcel(
                    certificate.getStudent().getEmail(),
                    certificate.getStudent().getName(),
                    user.getUniversity().getName(),
                    certificateUrl,
                    "Chứng Chỉ"
            );

            Notifications notifications = new Notifications();
            notifications.setUser(user);
            notifications.setTitle(NotificationType.CERTIFICATE_APPROVED.getName());
            notifications.setContent("Phòng đào tạo đã xác nhận chứng chỉ có số hiệu: "+ certificate.getDiplomaNumber());
            notifications.setType(NotificationType.CERTIFICATE_APPROVED);
            notifications.setDocumentType("CERTIFICATE");
            notifications.setDocumentId(certificate.getId());
            notificateService.save(notifications);

            User userDepartment = userService.findByDepartment(certificate.getStudent().getStudentClass().getDepartment());

            NotificationReceivers notificationReceivers = new NotificationReceivers();
            notificationReceivers.setNotification(notifications);
            notificationReceivers.setReceiverId(userDepartment.getId());
            notificationReceivers.setCreatedAt(vietnamTime.toLocalDateTime());
            notificationReceiverService.save(notificationReceivers);

            Wallet wallet= walletService.findByStudent(certificate.getStudent());
            if (wallet == null || wallet.getWalletAddress() == null) {
                throw new RuntimeException("Không tìm thấy ví của sinh viên");
            }
            //gửi token
            stUcoinService.transferToStudent(wallet.getWalletAddress(), new BigInteger("5").multiply(BigInteger.TEN.pow(18))); // 5 STUcoin (18 decimals)

            //gửi WebSocket
            NotificationResponse response = new NotificationResponse(
                    notificationReceivers.getId(),
                    notifications.getTitle(),
                    notifications.getContent(),
                    notifications.getType(),
                    false,
                    notifications.getDocumentType(),
                    notifications.getDocumentId(),
                    notificationReceivers.getCreatedAt()
            );
            notificationWebSocketSender.sendNotification(userDepartment.getId(), response);

        }

        List<Certificate> certificates = certificateRepository.findAllById(ids);
        certificateRepository.saveAll(certificates);

        // ghi log
        String ipAdress = auditLogService.getClientIp(httpServletRequest);
        Log log = new Log();
        log.setUser(auditLogService.getCurrentUser());
        log.setActionType(ActionType.VERIFIED);
        log.setEntityName(Entity.certificates);
        log.setEntityId(null);
        log.setDescription(LogTemplate.VERIFIED_CERTIFICATE.format(ids.size()));
        log.setCreatedAt(vietnamTime.toLocalDateTime());
        log.setIpAddress(ipAdress);
        logRepository.save(log);
    }

    // từ chối xác nhận chứng chỉ
    @Transactional
    @Auditable(action = ActionType.REJECTED, entity = Entity.certificates)
    public Certificate certificateRejected (Long idCertificate, User user) throws Exception {
        try {
            ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

            Certificate certificate = certificateRepository.findById(idCertificate)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chứng chỉ có id " + idCertificate));

            certificate.setStatus(Status.REJECTED);
            certificate.setUpdatedAt(vietnamTime.toLocalDateTime());
            AuditingContext.setDescription("Từ chối xác thực chứng chỉ số hiệu bằng: " + certificate.getDiplomaNumber());

            Notifications notifications = new Notifications();
            notifications.setUser(user);
            notifications.setTitle(NotificationType.CERTIFICATE_REJECTED.getName());
            notifications.setContent("Phòng đào tạo đã từ chối xác nhận chứng chỉ có số hiệu: "+ certificate.getDiplomaNumber());
            notifications.setType(NotificationType.CERTIFICATE_REJECTED);
            notifications.setDocumentType("CERTIFICATE");
            notifications.setDocumentId(certificate.getId());
            notificateService.save(notifications);

            User userDepartment = userService.findByDepartment(certificate.getStudent().getStudentClass().getDepartment());

            NotificationReceivers notificationReceivers = new NotificationReceivers();
            notificationReceivers.setNotification(notifications);
            notificationReceivers.setReceiverId(userDepartment.getId());
            notificationReceivers.setCreatedAt(vietnamTime.toLocalDateTime());
            notificationReceiverService.save(notificationReceivers);

            //gửi WebSocket thông báo realtime
            NotificationResponse response = new NotificationResponse(
                    notificationReceivers.getId(),
                    notifications.getTitle(),
                    notifications.getContent(),
                    notifications.getType(),
                    false,
                    notifications.getDocumentType(),
                    notifications.getDocumentId(),
                    notificationReceivers.getCreatedAt()
            );
            notificationWebSocketSender.sendNotification(userDepartment.getId(), response);


            return certificate;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // từ chối 1 list
    @Transactional
    public void rejectCertificates(List<Long> ids, User user) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        List<Certificate> certificates = certificateRepository.findAllById(ids);
        for (Certificate cert : certificates) {
            cert.setStatus(Status.REJECTED);
            cert.setUpdatedAt(vietnamTime.toLocalDateTime());

            Notifications notifications = new Notifications();
            notifications.setUser(user);
            notifications.setTitle(NotificationType.CERTIFICATE_REJECTED.getName());
            notifications.setContent("Phòng đào tạo đã từ chối xác nhận chứng chỉ có số hiệu: "+ cert.getDiplomaNumber());
            notifications.setType(NotificationType.CERTIFICATE_REJECTED);
            notifications.setDocumentType("CERTIFICATE");
            notifications.setDocumentId(cert.getId());
            notificateService.save(notifications);

            User userDepartment = userService.findByDepartment(cert.getStudent().getStudentClass().getDepartment());

            NotificationReceivers notificationReceivers = new NotificationReceivers();
            notificationReceivers.setNotification(notifications);
            notificationReceivers.setReceiverId(userDepartment.getId());
            notificationReceivers.setCreatedAt(vietnamTime.toLocalDateTime());
            notificationReceiverService.save(notificationReceivers);

            //gửi WebSocket
            NotificationResponse response = new NotificationResponse(
                    notificationReceivers.getId(),
                    notifications.getTitle(),
                    notifications.getContent(),
                    notifications.getType(),
                    false,
                    notifications.getDocumentType(),
                    notifications.getDocumentId(),
                    notificationReceivers.getCreatedAt()
            );
            notificationWebSocketSender.sendNotification(userDepartment.getId(), response);

        }

        certificateRepository.saveAll(certificates);

        // ghi log
        String ipAdress = auditLogService.getClientIp(httpServletRequest);
        Log log = new Log();
        log.setUser(auditLogService.getCurrentUser());
        log.setActionType(ActionType.REJECTED);
        log.setEntityName(Entity.certificates);
        log.setEntityId(null);
        log.setDescription(LogTemplate.REJECTED_CERTIFICATE.format(ids.size()));
        log.setCreatedAt(vietnamTime.toLocalDateTime());
        log.setIpAddress(ipAdress);
        logRepository.save(log);
    }


    public Set<String> findAllDiplomaNumbers(Collection<String> diplomaNumbers) {
        return new HashSet<>(certificateRepository.findExistingDiplomaNumbers(diplomaNumbers));
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<CertificateExcelDTO> getAllCertificateDTOs(String type) {
        List<Certificate> certificates = certificateRepository.findByStatus(type);
        AtomicInteger counter = new AtomicInteger(1);
        return certificates.stream()
                .map(certificate -> {
                    CertificateExcelDTO dto = convertToDTO(certificate);
                    dto.setStt(counter.getAndIncrement());
                    return dto;
                })
                .toList();

    }

    public CertificateExcelDTO convertToDTO(Certificate entity) {
        CertificateExcelDTO dto = new CertificateExcelDTO();
        dto.setStudentCode(entity.getStudent().getStudentCode());
        dto.setStudentName(entity.getStudent().getName());
        dto.setStudentClass(entity.getStudent().getStudentClass().getName());
        dto.setDepartmentName(entity.getStudent().getStudentClass().getDepartment().getName());
        dto.setIssueDate(entity.getIssueDate());
        dto.setGrantor(entity.getGrantor());
        dto.setSigner(entity.getSigner());
        dto.setDiplomaNumber(entity.getDiplomaNumber());
        dto.setStatus(entity.getStatus().getLabel());
        return dto;
    }

}
