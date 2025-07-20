package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.annotation.Auditable;
import com.example.blockchain.record.keeping.aspect.AuditingContext;
import com.example.blockchain.record.keeping.configs.Constants;
import com.example.blockchain.record.keeping.dtos.CertificateExcelDTO;
import com.example.blockchain.record.keeping.dtos.DegreeExcelDTO;
import com.example.blockchain.record.keeping.dtos.request.*;
import com.example.blockchain.record.keeping.enums.*;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.*;
import com.example.blockchain.record.keeping.response.*;
import com.example.blockchain.record.keeping.utils.PinataUploader;
import com.example.blockchain.record.keeping.utils.QrCodeUtil;
import com.example.blockchain.record.keeping.utils.RSAUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.web3j.protocol.Web3j;

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
public class DegreeService implements IDegreeService{

    private final DegreeRepository degreeRepository;
    private final EducationModelSevice educationModelSevice;
    private final DegreeTitleSevice degreeTitleSevice;
    private final RatingService ratingService;
    private final StudentService studentService;
    private final GraphicsTextWriter graphicsTextWriter;
    private final QrCodeUtil qrCodeUtil;
    private final BlockChainService blockChainService;
    private final BrevoApiEmailService brevoApiEmailService;
    private final AuditLogService auditLogService;
    private final ActionChangeRepository actionChangeRepository;
    private final LogRepository logRepository;
    private final HttpServletRequest httpServletRequest;
    private final UserService userService;
    private final NotificateService notificateService;
    private final NotificationReceiverService notificationReceiverService;
    private final WalletService walletService;
    private final STUcoinService stUcoinService;
    private final NotificationWebSocketSender notificationWebSocketSender;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RSAUtil rsaUtil;

    @Transactional
    @Auditable(action = ActionType.CREATED, entity = Entity.degrees)
    public Degree createDegree(DegreeRequest request, User user) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Student student = studentService.findById(request.getStudentId());

        Rating rating = ratingService.findById(request.getRatingId());
        EducationMode educationMode = educationModelSevice.findById(request.getEducationModeId());
        DegreeTitle degreeTitle = degreeTitleSevice.findById(request.getDegreeTitleId());

        LocalDate issueDate = LocalDate.parse(request.getIssueDate(), formatter);

        Degree degree = new Degree();
        degree.setStudent(student);
        degree.setRating(rating);
        degree.setDegreeTitle(degreeTitle);
        degree.setEducationMode(educationMode);
        degree.setGraduationYear(request.getGraduationYear());
        degree.setIssueDate(issueDate);
        degree.setTrainingLocation(request.getTrainingLocation());
        degree.setSigner(request.getSigner());
        degree.setDiplomaNumber(request.getDiplomaNumber());
        degree.setLotteryNumber(request.getLotteryNumber());
        degree.setBlockchainTxHash(null);
        degree.setIpfsUrl(null);
        degree.setQrCode(null);
        degree.setStatus(Status.PENDING);
        degree.setCreatedAt(vietnamTime.toLocalDateTime());
        degree.setUpdatedAt(vietnamTime.toLocalDateTime());

        // tạo ảnh
        DegreePrintData degreePrintData = new DegreePrintData();
        degreePrintData.setUniversityName(student.getStudentClass().getDepartment().getUniversity().getName());
        degreePrintData.setDegreeTitle("Bằng " +degreeTitle.getName());
        degreePrintData.setDepartmentName(student.getStudentClass().getDepartment().getName());
        degreePrintData.setName(student.getName());
        degreePrintData.setBirthDate(student.getBirthDate().format(formatter));
        degreePrintData.setGraduationYear(request.getGraduationYear());
        degreePrintData.setRating(rating.getName());
        degreePrintData.setEducationMode(educationMode.getName());
        degreePrintData.setDay(String.valueOf(issueDate.getDayOfMonth()));
        degreePrintData.setMonth(String.valueOf(issueDate.getMonthValue()));
        degreePrintData.setYear(String.valueOf(issueDate.getYear()));
        degreePrintData.setTrainingLocation(request.getTrainingLocation());
        degreePrintData.setSigner(request.getSigner());
        degreePrintData.setDiplomaNumber(request.getDiplomaNumber());
        degreePrintData.setLotteryNumber(request.getLotteryNumber());

        String image_url = graphicsTextWriter.drawDegreeText(degreePrintData);

        degree.setImageUrl(image_url);

        AuditingContext.setDescription("Tạo văn bằng cho một sinh viên, số hiệu bằng: " + degree.getDiplomaNumber());

        degreeRepository.save(degree);

        Notifications notifications = new Notifications();
        notifications.setUser(user);
        notifications.setTitle(NotificationType.DEGREE_CREATED.getName());
        notifications.setContent("Khoa "+ user.getDepartment().getName() +" đã tạo văn bằng có số hiệu: "+ degree.getDiplomaNumber());
        notifications.setType(NotificationType.DEGREE_CREATED);
        notifications.setDocumentType("DEGREE");
        notifications.setDocumentId(degree.getId());
        notificateService.save(notifications);

        User userUniversity = userService.findByUser(degree.getStudent().getStudentClass().getDepartment().getUniversity().getEmail());

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

        return degree;
    }


    @Override
    public Degree save(Degree degree) {
        return degreeRepository.save(degree);
    }

    @Override
    public boolean existsByStudent(Student student) {
        return degreeRepository.existsByStudentAndStatusNot(student, Status.REJECTED);
    }

    @Override
    public Degree findById(Long id) {
        return degreeRepository.findById(id).orElse(null);
    }

    @Override
    public boolean existsByIdAndStatus(Long id) {
        return degreeRepository.existsByIdAndStatus(id, Status.APPROVED);
    }

    @Override
    public Degree updateDegree(Long id, DegreeRequest request) {

        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        Rating rating = ratingService.findById(request.getRatingId());
        EducationMode educationMode = educationModelSevice.findById(request.getEducationModeId());
        DegreeTitle degreeTitle = degreeTitleSevice.findById(request.getDegreeTitleId());

        LocalDate issueDate = LocalDate.parse(request.getIssueDate(), formatter);

        Degree degree = findById(id);
        Degree degreeOld = auditLogService.cloneDegree(degree);

        degree.setRating(rating);
        degree.setDegreeTitle(degreeTitle);
        degree.setEducationMode(educationMode);
        degree.setGraduationYear(request.getGraduationYear());
        degree.setIssueDate(issueDate);
        degree.setTrainingLocation(request.getTrainingLocation());
        degree.setSigner(request.getSigner());
        degree.setDiplomaNumber(request.getDiplomaNumber());
        degree.setLotteryNumber(request.getLotteryNumber());
        degree.setUpdatedAt(vietnamTime.toLocalDateTime());

        // tạo ảnh
        DegreePrintData degreePrintData = new DegreePrintData();
        degreePrintData.setUniversityName(degree.getStudent().getStudentClass().getDepartment().getUniversity().getName());
        degreePrintData.setDegreeTitle("Bằng " +rating.getName());
        degreePrintData.setDepartmentName(degree.getStudent().getStudentClass().getDepartment().getName());
        degreePrintData.setName(degree.getStudent().getName());
        degreePrintData.setBirthDate(degree.getStudent().getBirthDate().format(formatter));
        degreePrintData.setGraduationYear(request.getGraduationYear());
        degreePrintData.setRating(rating.getName());
        degreePrintData.setEducationMode(educationMode.getName());
        degreePrintData.setDay(String.valueOf(issueDate.getDayOfMonth()));
        degreePrintData.setMonth(String.valueOf(issueDate.getMonthValue()));
        degreePrintData.setYear(String.valueOf(issueDate.getYear()));
        degreePrintData.setTrainingLocation(request.getTrainingLocation());
        degreePrintData.setSigner(request.getSigner());
        degreePrintData.setDiplomaNumber(request.getDiplomaNumber());
        degreePrintData.setLotteryNumber(request.getLotteryNumber());

        String image_url = graphicsTextWriter.drawDegreeText(degreePrintData);

        degree.setImageUrl(image_url);
        Degree degreeNew = degreeRepository.save(degree);

        String ipAdress = auditLogService.getClientIp(httpServletRequest);
        List<ActionChange> changes = auditLogService.compareObjects(null, degreeOld, degreeNew);
        if (!changes.isEmpty()) {// nếu khác old mới lưu
            Log log = new Log();
            log.setUser(auditLogService.getCurrentUser());
            log.setActionType(ActionType.UPDATED);
            log.setEntityName(Entity.degrees);
            log.setEntityId(id);
            log.setDescription(LogTemplate.UPDATE_DEGREES.format(degree.getDiplomaNumber()));
            log.setIpAddress(ipAdress);
            log.setCreatedAt(vietnamTime.toLocalDateTime());

            log = logRepository.save(log);

            for (ActionChange change : changes) {
                change.setLog(log);
            }
            actionChangeRepository.saveAll(changes);
        }

        return degreeNew;
    }

    @Override
    public List<Degree> findAll() {
        return degreeRepository.findAll();
    }

    @Override
    public List<Degree> saveAll(List<Degree> degreeList) {
        return degreeRepository.saveAll(degreeList);
    }

    @Override
    public Degree findbyDiplomaNumber(String diplomaNumber) {
        return degreeRepository.findByDiplomaNumber(diplomaNumber).orElse(null);
    }

    @Override
    public Degree findByLotteryNumber(String lotteryNumber) {
        return degreeRepository.findByLotteryNumber(lotteryNumber).orElse(null);
    }

    @Override
    public List<Degree> listAllDegreeOfUniversity(Long universittyId, String departmentName, String className, String studentCode, String studentName,String graduationYear, String diplomaNumber, int limit, int offset){
        return degreeRepository.listAllDegreeOfUniversity(universittyId,departmentName,className,studentCode,studentName, graduationYear,diplomaNumber, limit,offset);
    }

    @Override
    public List<Degree> listAllDegreeOfUniversityAndStatus(Long universittyId, String departmentName, String className, String studentCode, String studentName,String graduationYear,String diplomaNumber, String status,  int limit, int offset){
        return degreeRepository.listDegreeOfUniversityAndStatus(universittyId,departmentName,className,studentCode,studentName, graduationYear,diplomaNumber, status, limit, offset);
    }

    @Override
    public List<Degree> listAllDegreeOfDepartment(Long departmentId, String className, String studentCode, String studentName,String graduationYear, String diplomaNumber,  int limit, int offset){
        return degreeRepository.listAllDegreeOfDepartment(departmentId,className,studentCode,studentName, graduationYear,diplomaNumber, limit, offset);
    }

    @Override
    public List<Degree> listAllDegreeOfDepartmentAndStatus( Long departmentId, String className, String studentCode, String studentName,String graduationYear,String diplomaNumber, String status,  int limit, int offset){
        return degreeRepository.listAllDegreeOfDepartmentAndStatus(departmentId,className,studentCode,studentName, graduationYear,diplomaNumber, status, limit, offset);
    }

    @Override
    public List<Degree> listAllDegree(String universityName, String departmentName, String className, String studentCode, String studentName, String graduationYear, String diplomaNumber) {
        return degreeRepository.listAllDegree(universityName, departmentName,className,studentCode,studentName, graduationYear,diplomaNumber);
    }

    @Override
    public Degree findByIdAndStatus(Long id, Status status) {
        return degreeRepository.findByIdAndStatus(id, status);
    }

    @Override
    public Degree findByIpfsUrl(String ipfsUrl) {
        return degreeRepository.findByIpfsUrl(ipfsUrl);
    }

    @Override
    public DegreeClassificationStatisticsResponse degreeClassificationStatisticsOfUniversity(Long universityId) {
        DegreeClassificationStatisticsRequest result = degreeRepository.getDegreeClassificationStatistics(universityId);

        DegreeClassificationStatisticsResponse response = new DegreeClassificationStatisticsResponse(
                result.getExcellent(),
                result.getVeryGood(),
                result.getGood(),
                result.getAverage()
        );
        return response;
    }

    @Override
    public DegreeClassificationStatisticsResponse degreeClassificationStatisticsOfDepartment(Long departmentId) {
        DegreeClassificationStatisticsRequest result = degreeRepository.getDegreeClassificationStatisticsOfDepartment(departmentId);

        DegreeClassificationStatisticsResponse response = new DegreeClassificationStatisticsResponse(
                result.getExcellent(),
                result.getVeryGood(),
                result.getGood(),
                result.getAverage()
        );
        return response;
    }

    @Override
    public List<DegreeClassificationByYearResponse> getDegreeClassificationByUniversityAndLast5Years(Long universityId) {
        List<Object[]> raw =  degreeRepository.getDegreeClassificationByUniversityAndLast5Years(universityId);

        return raw.stream().map(row -> new DegreeClassificationByYearResponse(
                ((Number) row[0]).intValue(),
                ((Number) row[1]).longValue(),
                ((Number) row[2]).longValue(),
                ((Number) row[3]).longValue()
        )).collect(Collectors.toList());
    }

    @Override
    public List<DegreeClassificationByYearResponse> getDegreeClassificationByDepartmentAndLast5Years(Long departmentId) {
        List<Object[]> raw =  degreeRepository.getDegreeClassificationByDepartmentAndLast5Years(departmentId);

        return raw.stream().map(row -> new DegreeClassificationByYearResponse(
                ((Number) row[0]).intValue(),
                ((Number) row[1]).longValue(),
                ((Number) row[2]).longValue(),
                ((Number) row[3]).longValue()
        )).collect(Collectors.toList());
    }

    public List<DegreeResponse> degreeOfStudent(Long studentId) {
        Optional<Degree> degree = degreeRepository.degreeOfStudent(studentId);

        if (degree.isEmpty()) {
            return List.of();
        }
        Degree d = degree.get();
        DegreeResponse response = new DegreeResponse(
                d.getId(),
                d.getStudent().getName(),
                d.getStudent().getStudentClass().getName(),
                d.getStudent().getStudentClass().getDepartment().getName(),
                d.getIssueDate(),
                d.getStatus().getLabel(),
                d.getGraduationYear(),
                d.getDiplomaNumber(),
                d.getUpdatedAt()
        );
        return List.of(response);
    }

    @Override
    public boolean existByDiplomanumber(Long universityId, String diplomaNumber) {
        Degree degree = degreeRepository.existByDiplomaNumber(universityId,diplomaNumber);
        if(degree == null){
            return false;
        }
        return true;
    }

    @Override
    public boolean existByLotteryNumber(Long universityId, String lotteryNumber) {
        Degree degree = degreeRepository.existByLotteryNumber(universityId,lotteryNumber);
        if(degree == null){
            return false;
        }
        return true;
    }

    @Override
    public boolean existByDiplomanumberIngnoreId(Long universityId, String diplomaNumber, Long degreeId) {
        Degree degree = degreeRepository.existByDiplomaNumberIgnoreId(universityId,diplomaNumber,degreeId);
        if(degree == null){
            return false;
        }
        return true;
    }

    @Override
    public boolean existByLotteryNumberIngnoreId(Long universityId, String lotteryNumber, Long degreeId) {
        Degree degree = degreeRepository.existByLotteryNumberIgnoreId(universityId,lotteryNumber,degreeId);
        if(degree == null){
            return false;
        }
        return true;
    }

    @Override
    public long countAllDegreeOfUniversity(Long universityId, String departmentName, String className, String studentCode, String studentName, String graduationYear, String diplomaNumber) {
        return degreeRepository.countAllDegreeOfUniversity(universityId, departmentName, className, studentCode, studentName, graduationYear, diplomaNumber);
    }

    @Override
    public long countAllDegreeOfUniversityAndStatus(Long universityId, String departmentName, String className, String studentCode, String studentName, String graduationYear, String diplomaNumber, String status) {
        return degreeRepository.countDegreeOfUniversityAndStatus(universityId, departmentName, className, studentCode, studentName, graduationYear, diplomaNumber, status);
    }

    @Override
    public long countAllDegreeOfDepartment(Long departmentId, String className, String studentCode, String studentName, String graduationYear, String diplomaNumber) {
        return degreeRepository.countAllDegreeOfDepartment(departmentId, className, studentCode, studentName, graduationYear, diplomaNumber);
    }

    @Override
    public long countDegreeOfDepartmentAndStatus(Long departmentId, String className, String studentCode, String studentName, String graduationYear, String diplomaNumber, String status) {
        return degreeRepository.countDegreeOfDepartmentAndStatus(departmentId, className, studentCode, studentName, graduationYear, diplomaNumber, status);
    }

    @Override
    @Auditable(action = ActionType.DELETED, entity = Entity.degrees)
    public Degree delete(Long id) {
        Degree degree = degreeRepository.findById(id).orElse(null);
        AuditingContext.setDescription("Xóa văn bằng có số hệu :" + degree.getDiplomaNumber());
        degreeRepository.delete(id);
        return degree;
    }

    public Map<String, Boolean> checkStudentsGrantedDegree(Set<String> studentCodes) {
        List<String> existingCodes = degreeRepository.findStudentCodesWithDegree(studentCodes);
        Map<String, Boolean> result = new HashMap<>();
        for (String code : studentCodes) {
            result.put(code, existingCodes.contains(code));
        }
        return result;
    }

    public Set<String> findAllDiplomaNumbers(Collection<String> diplomaNumbers) {
        return new HashSet<>(degreeRepository.findExistingDiplomaNumbers(diplomaNumbers));
    }

    public Set<String> findAllLotteryNumbers(Collection<String> lotteryNumbers) {
        return new HashSet<>(degreeRepository.findExistingLotteryNumbers(lotteryNumbers));
    }

    // xác nhận 1 van bang
    @Transactional
    @Auditable(action = ActionType.VERIFIED, entity = Entity.degrees)
    public Degree degreeValidation (User user,Long degreeId) throws Exception {
        try {
            ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            Degree degree = degreeRepository.findById(degreeId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy văn bằng có id " + degreeId));

            String imageUrl = graphicsTextWriter.degreeValidation(degree.getImageUrl(), user.getUniversity().getSealImageUrl());
            degree.setImageUrl(imageUrl);

            String ipfsUrl = PinataUploader.uploadFromUrlToPinata(imageUrl);
            degree.setIpfsUrl(ipfsUrl);

            //đường dẫn chứng chỉ IPFS
            String certificateUrl = Constants.VERIFY_URL + ipfsUrl + "&type=degree";

            String qrBase64 = qrCodeUtil.generateQRCodeBase64(certificateUrl, 250, 250);
            degree.setQrCode(qrBase64);
            degree.setStatus(Status.APPROVED);
            degree.setUpdatedAt(vietnamTime.toLocalDateTime());

            // lấy thông tin vb lưu block
            DegreeBlockchainRequest request = new DegreeBlockchainRequest(
                    degree.getStudent().getName(),
                    user.getUniversity().getName(),
                    degree.getIssueDate().format(formatter),
                    degree.getDiplomaNumber(),
                    degree.getLotteryNumber(),
                    ipfsUrl
            );
            String base64PrivateKey = user.getUniversity().getPrivateKey();
            PrivateKey privateKey = RSAKeyPairGenerator.getPrivateKeyFromBase64(base64PrivateKey);
            String json = objectMapper.writeValueAsString(request);
            String encryptedHex = rsaUtil.encryptWithPrivateKeyToHex(json, privateKey);

            //gửi blockchain và lấy txHash naof goij thi mo
            String txHash = blockChainService.issue(encryptedHex);
            degree.setBlockchainTxHash(txHash);

            // gửi emial
            brevoApiEmailService.sendEmailsToStudentsExcel(
                    degree.getStudent().getEmail(),
                    degree.getStudent().getName(),
                    user.getUniversity().getName(),
                    certificateUrl,
                    "Văn Bằng");

            AuditingContext.setDescription("Xác thực văn bằng số hiệu bằng: " + degree.getDiplomaNumber());

            Notifications notifications = new Notifications();
            notifications.setUser(user);
            notifications.setTitle(NotificationType.DEGREE_APPROVED.getName());
            notifications.setContent("Phòng đào tạo đã xác nhận văn bằng có số hiệu: "+ degree.getDiplomaNumber());
            notifications.setType(NotificationType.DEGREE_APPROVED);
            notifications.setDocumentType("DEGREE");
            notifications.setDocumentId(degree.getId());
            notificateService.save(notifications);

            User userDepartment = userService.findByDepartment(degree.getStudent().getStudentClass().getDepartment());

            NotificationReceivers notificationReceivers = new NotificationReceivers();
            notificationReceivers.setNotification(notifications);
            notificationReceivers.setReceiverId(userDepartment.getId());
            notificationReceivers.setCreatedAt(vietnamTime.toLocalDateTime());
            notificationReceiverService.save(notificationReceivers);

            Wallet wallet= walletService.findByStudent(degree.getStudent());
            if (wallet == null || wallet.getWalletAddress() == null) {
                throw new RuntimeException("Không tìm thấy ví của sinh viên");
            }
            //gửi token
            stUcoinService.transferToStudent(wallet.getWalletAddress(), new BigInteger("5").multiply(BigInteger.TEN.pow(18))); // 5 STUcoin (18 decimals)
            BigInteger amountBI = BigInteger.valueOf(5).multiply(BigInteger.TEN.pow(18));
            walletService.updateWalletCoinAmount(wallet,amountBI,true);
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

            return degree;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // xác nhận 1 list
    @Transactional
    public void approveDegreeList(List<Long> ids, User user, HttpServletRequest request) throws Exception {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Long id : ids) {
            Degree degree = degreeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy văn bằng có id " + id));

            String imageUrl = graphicsTextWriter.degreeValidation(degree.getImageUrl(), user.getUniversity().getSealImageUrl());
            degree.setImageUrl(imageUrl);

            //IPFS & QR
            String ipfsUrl = PinataUploader.uploadFromUrlToPinata(imageUrl);
            degree.setIpfsUrl(ipfsUrl);
            String verifyUrl = Constants.VERIFY_URL + ipfsUrl + "&type=degree";
            String qrBase64 = qrCodeUtil.generateQRCodeBase64(verifyUrl, 250, 250);
            degree.setQrCode(qrBase64);
            degree.setStatus(Status.APPROVED);
            degree.setUpdatedAt(vietnamTime.toLocalDateTime());

            DegreeBlockchainRequest bcRequest = new DegreeBlockchainRequest(
                    degree.getStudent().getName(),
                    user.getUniversity().getName(),
                    degree.getIssueDate().format(formatter),
                    degree.getDiplomaNumber(),
                    degree.getLotteryNumber(),
                    ipfsUrl
            );
            String privateKeyBase64 = user.getUniversity().getPrivateKey();
            PrivateKey privateKey = RSAKeyPairGenerator.getPrivateKeyFromBase64(privateKeyBase64);
            String json = objectMapper.writeValueAsString(bcRequest);
            String encryptedHex = rsaUtil.encryptWithPrivateKeyToHex(json, privateKey);

            String txHash = blockChainService.issue(encryptedHex);
            degree.setBlockchainTxHash(txHash);

            //email NÀO CHẠY MỞ
//            brevoApiEmailService.sendEmailsToStudentsExcel(
//                    degree.getStudent().getEmail(),
//                    degree.getStudent().getName(),
//                    user.getUniversity().getName(),
//                    verifyUrl,
//                    "Văn Bằng");

            Notifications notifications = new Notifications();
            notifications.setUser(user);
            notifications.setTitle(NotificationType.DEGREE_APPROVED.getName());
            notifications.setContent("Phòng đào tạo đã xác nhận văn bằng có số hiệu: "+ degree.getDiplomaNumber());
            notifications.setType(NotificationType.DEGREE_APPROVED);
            notifications.setDocumentType("DEGREE");
            notifications.setDocumentId(degree.getId());
            notificateService.save(notifications);

            User userDepartment = userService.findByDepartment(degree.getStudent().getStudentClass().getDepartment());

            NotificationReceivers notificationReceivers = new NotificationReceivers();
            notificationReceivers.setNotification(notifications);
            notificationReceivers.setReceiverId(userDepartment.getId());
            notificationReceivers.setCreatedAt(vietnamTime.toLocalDateTime());
            notificationReceiverService.save(notificationReceivers);

            Wallet wallet= walletService.findByStudent(degree.getStudent());
            if (wallet == null || wallet.getWalletAddress() == null) {
                throw new RuntimeException("Không tìm thấy ví của sinh viên");
            }
            //gửi token
            stUcoinService.transferToStudent(wallet.getWalletAddress(), new BigInteger("5").multiply(BigInteger.TEN.pow(18))); // 5 STUcoin (18 decimals)
            BigInteger amountBI = BigInteger.valueOf(5).multiply(BigInteger.TEN.pow(18));
            walletService.updateWalletCoinAmount(wallet,amountBI,true);
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
        List<Degree> degrees = degreeRepository.findAllById(ids);
        degreeRepository.saveAll(degrees);

        //log
        String ipAdress = auditLogService.getClientIp(httpServletRequest);
        Log log = new Log();
        log.setUser(auditLogService.getCurrentUser());
        log.setActionType(ActionType.VERIFIED);
        log.setEntityName(Entity.degrees);
        log.setEntityId(null);
        log.setDescription(LogTemplate.VERIFIED_DEGREE.format(ids.size()));
        log.setIpAddress(ipAdress);
        log.setCreatedAt(vietnamTime.toLocalDateTime());
        logRepository.save(log);
    }

    //từ chối xác nhận van bang
    @Transactional
    @Auditable(action = ActionType.REJECTED, entity = Entity.degrees)
    public Degree degreeRejected (Long degreeId, User user) throws Exception {
        try {
            ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

            Degree degree = degreeRepository.findById(degreeId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy văn bằng có id " + degreeId));

            degree.setStatus(Status.REJECTED);
            degree.setUpdatedAt(vietnamTime.toLocalDateTime());
            AuditingContext.setDescription("Từ chối xác thực văn bằng số hiệu văn bằng: " + degree.getDiplomaNumber());

            Notifications notifications = new Notifications();
            notifications.setUser(user);
            notifications.setTitle(NotificationType.CERTIFICATE_REJECTED.getName());
            notifications.setContent("Phòng đào tạo đã từ chối xác nhận văn bằng có số hiệu: "+ degree.getDiplomaNumber());
            notifications.setType(NotificationType.CERTIFICATE_REJECTED);
            notifications.setDocumentType("DEGREE");
            notifications.setDocumentId(degree.getId());
            notificateService.save(notifications);

            User userDepartment = userService.findByDepartment(degree.getStudent().getStudentClass().getDepartment());

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

            return degree;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //từ chối 1 list van bang
    @Transactional
    public void rejectDegreeList(List<Long> ids, User user) throws Exception {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        List<Degree> degrees = degreeRepository.findAllById(ids);
        for (Degree degree : degrees) {
            degree.setStatus(Status.REJECTED);
            degree.setUpdatedAt(vietnamTime.toLocalDateTime());

            Notifications notifications = new Notifications();
            notifications.setUser(user);
            notifications.setTitle(NotificationType.DEGREE_REJECTED.getName());
            notifications.setContent("Phòng đào tạo đã từ chối xác nhận văn bằng có số hiệu: "+ degree.getDiplomaNumber());
            notifications.setType(NotificationType.DEGREE_REJECTED);
            notifications.setDocumentType("DEGREE");
            notifications.setDocumentId(degree.getId());
            notificateService.save(notifications);

            User userDepartment = userService.findByDepartment(degree.getStudent().getStudentClass().getDepartment());

            NotificationReceivers notificationReceivers = new NotificationReceivers();
            notificationReceivers.setNotification(notifications);
            notificationReceivers.setReceiverId(userDepartment.getId());
            notificationReceivers.setCreatedAt(vietnamTime.toLocalDateTime());
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

        degreeRepository.saveAll(degrees);

        //log
        String ipAdress = auditLogService.getClientIp(httpServletRequest);
        Log log = new Log();
        log.setUser(auditLogService.getCurrentUser());
        log.setActionType(ActionType.REJECTED);
        log.setEntityName(Entity.degrees);
        log.setEntityId(null);
        log.setDescription(LogTemplate.REJECTED_DEGREES.format(ids.size()));
        log.setIpAddress(ipAdress);
        log.setCreatedAt(vietnamTime.toLocalDateTime());
        logRepository.save(log);
    }


    public List<FacultyDegreeStatisticResponse> getFacultyDegreeStatistics(Long universityId) {
        List<FacultyDegreeStatisticRequest> results = degreeRepository.getFacultyDegreeStatistics(universityId);

        List<FacultyDegreeStatisticResponse> response = new ArrayList<>();
        for (FacultyDegreeStatisticRequest row : results) {
            FacultyDegreeStatisticResponse a = new FacultyDegreeStatisticResponse(
                    row.getDepartmentName(),
                    row.getDegreePending(),
                    row.getDegreeApproved(),
                    row.getDegreeRejected(),
                    row.getCertificatePending(),
                    row.getCertificateApproved(),
                    row.getCertificateRejected()
            );
            response.add(a);
        }

        return response;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<DegreeExcelDTO> getAllDegreeDTOs(String type) {

        List<Degree> degreeList = degreeRepository.findByStatus(type);
        AtomicInteger counter = new AtomicInteger(1);
        return degreeList.stream()
                .map(certificate -> {
                    DegreeExcelDTO dto = convertToDTO(certificate);
                    dto.setStt(counter.getAndIncrement());
                    return dto;
                })
                .toList();
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<DegreeExcelDTO> getDegreeWithIdDTOs(List<Long> ids) {
        List<Degree> degreeList = new ArrayList<>();
        for (Long id :ids){
            Degree degree = degreeRepository.findById(id).orElse(null);
            degreeList.add(degree);
        }
        AtomicInteger counter = new AtomicInteger(1);
        return degreeList.stream()
                .map(degree -> {
                    DegreeExcelDTO dto = convertToDTO(degree);
                    dto.setStt(counter.getAndIncrement());
                    return dto;
                })
                .toList();
    }

    public DegreeExcelDTO convertToDTO(Degree entity) {
        DegreeExcelDTO dto = new DegreeExcelDTO();
        dto.setStudentCode(entity.getStudent().getStudentCode());
        dto.setStudentName(entity.getStudent().getName());
        dto.setStudentClass(entity.getStudent().getStudentClass().getName());
        dto.setDepartmentName(entity.getStudent().getStudentClass().getDepartment().getName());
        dto.setIssueDate(entity.getIssueDate());
        dto.setGraduationYear(entity.getGraduationYear());
        dto.setRating(entity.getRating().getName());
        dto.setDegreeTitle(entity.getDegreeTitle().getName());
        dto.setEducationMode(entity.getEducationMode().getName());
        dto.setSigner(entity.getSigner());
        dto.setDiplomaNumber(entity.getDiplomaNumber());
        dto.setLotteryNumber(entity.getLotteryNumber());
        dto.setStatus(entity.getStatus().getLabel());
        return dto;
    }
}

