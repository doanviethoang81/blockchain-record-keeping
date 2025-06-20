package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.configs.Constants;
import com.example.blockchain.record.keeping.dtos.request.*;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.CertificateRepository;
import com.example.blockchain.record.keeping.repositorys.DegreeRepository;
import com.example.blockchain.record.keeping.repositorys.DegreeTitleRepository;
import com.example.blockchain.record.keeping.repositorys.StudentRepository;
import com.example.blockchain.record.keeping.response.FacultyDegreeStatisticResponse;
import com.example.blockchain.record.keeping.utils.PinataUploader;
import com.example.blockchain.record.keeping.utils.QrCodeUtil;
import com.example.blockchain.record.keeping.utils.RSAUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RSAUtil rsaUtil;

    @Transactional
    public void createDegree(DegreeRequest request) {
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
        degreeRepository.save(degree);
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
        return degreeRepository.save(degree);
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
    public List<Degree> listAllDegreeOfUniversity(Long universittyId, String departmentName, String className, String studentCode, String studentName,String graduationYear) {
        return degreeRepository.listAllDegreeOfUniversity(universittyId,departmentName,className,studentCode,studentName, graduationYear);
    }

    @Override
    public List<Degree> listAllDegreeOfUniversityAndStatus(Long universittyId, String departmentName, String className, String studentCode, String studentName,String graduationYear, String status) {
        return degreeRepository.listDegreeOfUniversity(universittyId,departmentName,className,studentCode,studentName, graduationYear, status);
    }

    @Override
    public List<Degree> listAllDegreeOfDepartment(Long departmentId, String className, String studentCode, String studentName,String graduationYear) {
        return degreeRepository.listAllDegreeOfDepartment(departmentId,className,studentCode,studentName, graduationYear);
    }

    @Override
    public List<Degree> listAllDegreeOfDepartmentAndStatus( Long departmentId, String className, String studentCode, String studentName,String graduationYear, String status) {
        return degreeRepository.listAllDegreeOfDepartmentAndStatus(departmentId,className,studentCode,studentName, graduationYear, status);
    }

    @Override
    public List<Degree> listAllDegree(String universityName, String departmentName, String className, String studentCode, String studentName, String graduationYear) {
        return degreeRepository.listAllDegree(universityName, departmentName,className,studentCode,studentName, graduationYear);
    }

    @Override
    public Degree findByIdAndStatus(Long id) {
        return degreeRepository.findByIdAndStatus(id, Status.APPROVED);
    }

    @Override
    public Degree findByIpfsUrl(String ipfsUrl) {
        return degreeRepository.findByIpfsUrl(ipfsUrl);
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
    public void degreeValidation (University university,Long degreeId) throws Exception {
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
            // gửi emial
            brevoApiEmailService.sendEmailsToStudentsExcel(
                    degree.getStudent().getEmail(),
                    degree.getStudent().getName(),
                    university.getName(),
                    certificateUrl,
                    "Văn bằng");

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

//            String txHash = "123"; //sua
            degree.setBlockchainTxHash(txHash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //từ chối xác nhận van bang
    @Transactional
    public void degreeRejected (University university,Long degreeId) throws Exception {
        try {
            ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

            Degree degree = degreeRepository.findById(degreeId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy văn bằng có id " + degreeId));

            degree.setStatus(Status.REJECTED);
            degree.setUpdatedAt(vietnamTime.toLocalDateTime());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



    public List<FacultyDegreeStatisticResponse> getFacultyDegreeStatistics(Long universityId) {
        List<FacultyDegreeStatisticRequest> results = degreeRepository.getFacultyDegreeStatistics(universityId);

        List<FacultyDegreeStatisticResponse> response = new ArrayList<>();
        for (FacultyDegreeStatisticRequest row : results) {
            FacultyDegreeStatisticResponse a = new FacultyDegreeStatisticResponse(
                    row.getDepartmentName(),
                    row.getValidatedDegreeCount(),
                    row.getNotValidatedDegreeCount(),
                    row.getValidatedCertificateCount(),
                    row.getNotValidatedCertificateCount()
            );
            response.add(a);
        }

        return response;
    }



}

