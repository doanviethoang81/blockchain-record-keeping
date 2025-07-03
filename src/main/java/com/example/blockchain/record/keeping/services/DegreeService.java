package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.annotation.Auditable;
import com.example.blockchain.record.keeping.configs.Constants;
import com.example.blockchain.record.keeping.dtos.request.*;
import com.example.blockchain.record.keeping.enums.ActionType;
import com.example.blockchain.record.keeping.enums.Entity;
import com.example.blockchain.record.keeping.enums.LogTemplate;
import com.example.blockchain.record.keeping.enums.Status;
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

import java.security.PrivateKey;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RSAUtil rsaUtil;

    @Transactional
    @Auditable(action = ActionType.CREATED, entity = Entity.degrees)
    public Degree createDegree(DegreeRequest request) {
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
        return degreeRepository.save(degree);
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
            log.setDescription(LogTemplate.UPDATE_DEGREES.getName());
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
    public List<Degree> listAllDegreeOfUniversity(Long universittyId, String departmentName, String className, String studentCode, String studentName,String graduationYear, String diplomaNumber) {
        return degreeRepository.listAllDegreeOfUniversity(universittyId,departmentName,className,studentCode,studentName, graduationYear,diplomaNumber);
    }

    @Override
    public List<Degree> listAllDegreeOfUniversityAndStatus(Long universittyId, String departmentName, String className, String studentCode, String studentName,String graduationYear,String diplomaNumber, String status) {
        return degreeRepository.listDegreeOfUniversity(universittyId,departmentName,className,studentCode,studentName, graduationYear,diplomaNumber, status);
    }

    @Override
    public List<Degree> listAllDegreeOfDepartment(Long departmentId, String className, String studentCode, String studentName,String graduationYear, String diplomaNumber) {
        return degreeRepository.listAllDegreeOfDepartment(departmentId,className,studentCode,studentName, graduationYear,diplomaNumber);
    }

    @Override
    public List<Degree> listAllDegreeOfDepartmentAndStatus( Long departmentId, String className, String studentCode, String studentName,String graduationYear,String diplomaNumber, String status) {
        return degreeRepository.listAllDegreeOfDepartmentAndStatus(departmentId,className,studentCode,studentName, graduationYear,diplomaNumber, status);
    }

    @Override
    public List<Degree> listAllDegree(String universityName, String departmentName, String className, String studentCode, String studentName, String graduationYear, String diplomaNumber) {
        return degreeRepository.listAllDegree(universityName, departmentName,className,studentCode,studentName, graduationYear,diplomaNumber);
    }

    @Override
    public Degree findByIdAndStatus(Long id) {
        return degreeRepository.findByIdAndStatus(id, Status.APPROVED);
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

    // xác nhận them dau moc van bang
    @Transactional
    @Auditable(action = ActionType.VERIFIED, entity = Entity.degrees)
    public Degree degreeValidation (University university,Long degreeId) throws Exception {
        try {
            ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            Degree degree = degreeRepository.findById(degreeId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy văn bằng có id " + degreeId));

            String imageUrl = graphicsTextWriter.degreeValidation(degree.getImageUrl(), university.getSealImageUrl());
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
                    university.getName(),
                    degree.getIssueDate().format(formatter),
                    degree.getDiplomaNumber(),
                    degree.getLotteryNumber(),
                    ipfsUrl
            );
            String base64PrivateKey = university.getPrivateKey();
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
                    university.getName(),
                    certificateUrl,
                    "Văn bằng");
            return degree;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // xác nhận 1 list
    @Transactional
    public void approveDegreeList(List<Long> ids, University university, HttpServletRequest request) throws Exception {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Long id : ids) {
            Degree degree = degreeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy văn bằng có id " + id));

            String imageUrl = graphicsTextWriter.degreeValidation(degree.getImageUrl(), university.getSealImageUrl());
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
                    university.getName(),
                    degree.getIssueDate().format(formatter),
                    degree.getDiplomaNumber(),
                    degree.getLotteryNumber(),
                    ipfsUrl
            );
            String privateKeyBase64 = university.getPrivateKey();
            PrivateKey privateKey = RSAKeyPairGenerator.getPrivateKeyFromBase64(privateKeyBase64);
            String json = objectMapper.writeValueAsString(bcRequest);
            String encryptedHex = rsaUtil.encryptWithPrivateKeyToHex(json, privateKey);

            String txHash = blockChainService.issue(encryptedHex);
            degree.setBlockchainTxHash(txHash);

            //email
            brevoApiEmailService.sendEmailsToStudentsExcel(
                    degree.getStudent().getEmail(),
                    degree.getStudent().getName(),
                    university.getName(),
                    verifyUrl,
                    "Văn bằng");
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
    public Degree degreeRejected (Long degreeId) throws Exception {
        try {
            ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

            Degree degree = degreeRepository.findById(degreeId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy văn bằng có id " + degreeId));

            degree.setStatus(Status.REJECTED);
            degree.setUpdatedAt(vietnamTime.toLocalDateTime());
            return degree;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //từ chối 1 list van bang
    @Transactional
    public void rejectDegreeList(List<Long> ids) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        List<Degree> degrees = degreeRepository.findAllById(ids);
        for (Degree degree : degrees) {
            degree.setStatus(Status.REJECTED);
            degree.setUpdatedAt(vietnamTime.toLocalDateTime());
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
}

