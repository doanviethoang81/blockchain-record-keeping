package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.request.DegreePrintData;
import com.example.blockchain.record.keeping.dtos.request.DegreeRequest;
import com.example.blockchain.record.keeping.enums.Status;
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
        degreePrintData.setDegreeTitle("Bằng " +rating.getName());
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



}

