package com.example.blockchain.record.keeping.services;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.excel.metadata.CellExtra;
import com.example.blockchain.record.keeping.dtos.CertificateExcelRowDTO;
import com.example.blockchain.record.keeping.dtos.request.CertificatePrintData;
import com.example.blockchain.record.keeping.dtos.request.StudentExcelRowRequest;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.exceptions.BadRequestException;
import com.example.blockchain.record.keeping.exceptions.ListBadRequestException;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.CertificateRepository;
import com.example.blockchain.record.keeping.repositorys.StudentRepository;
import com.example.blockchain.record.keeping.response.ApiResponseBuilder;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CertificateExcelListener extends AnalysisEventListener<CertificateExcelRowDTO> {

    private final UniversityService universityService;
    private final StudentService studentService;
    private Long departmentId = 0L;
    private final UniversityCertificateType universityCertificateType;
    private final CertificateService certificateService;
    private final GraphicsTextWriter graphicsTextWriter;

    public CertificateExcelListener(
            UniversityService universityService,
            StudentService studentService,
            Long departmentId,
            UniversityCertificateType universityCertificateType,
            CertificateService certificateService, GraphicsTextWriter graphicsTextWriter) {
        this.universityService = universityService;
        this.studentService = studentService;
        this.departmentId = departmentId;
        this.universityCertificateType = universityCertificateType;
        this.certificateService = certificateService;
        this.graphicsTextWriter = graphicsTextWriter;
    }

    private final List<CertificateExcelRowDTO> rows = new ArrayList<>();

    @Override
    public void invoke(CertificateExcelRowDTO data, AnalysisContext context) {
        rows.add(data);
    }

    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        super.onException(exception, context);
    }

    @Override
    public void extra(CellExtra extra, AnalysisContext context) {
        super.extra(extra, context);
    }

    @Override
    @Transactional
    public void doAfterAllAnalysed(AnalysisContext context) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        List<String> errors = new ArrayList<>();
        Set<String> duplicateStudentCodes = new HashSet<>();
        Set<String> duplicateDiplomaNumber = new HashSet<>();

        if (rows.size() > 1000) {
            throw new BadRequestException("Chỉ cho phép tối đa cấp 1000 chứng chỉ/lần import");
        }

        //thu thập tất cả mã sinh viên
        Set<String> allStudentCodes = rows.stream()
                .map(CertificateExcelRowDTO::getStudentCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        //tìm tất cả sinh viên 1 lần
        Map<String, Student> studentMap = studentService
                .findByStudentCodesOfDepartment(departmentId, allStudentCodes)
                .stream()
                .collect(Collectors.toMap(Student::getStudentCode, s -> s));

        //tìm tất cả certificate đã có để check duplicate
        Map<String, Boolean> existingCertificates = certificateService
                .findCertificatesOfStudentsByType(
                        studentMap.values().stream().map(Student::getId).collect(Collectors.toSet()),
                        universityCertificateType.getCertificateType().getId()
                );

        List<Certificate> certificatesToSave = new ArrayList<>();
        List<CertificatePrintData> printDataList = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            CertificateExcelRowDTO row = rows.get(i);
            int rowIndex = i + 1;

            if (!duplicateStudentCodes.add(row.getStudentCode())) {
                errors.add("Dòng " + rowIndex + ": Trùng mã sinh viên trong file");
                continue;
            }

            if (!duplicateDiplomaNumber.add(row.getDiplomaNumber())) {
                errors.add("Dòng " + rowIndex + ": Trùng số hiệu bằng trong file");
                continue;
            }

            if (row.getStudentCode() == null || row.getStudentCode().isBlank()) {
                errors.add("Dòng " + rowIndex + ": Mã số sinh viên không được để trống");
                continue;
            }

            if (row.getIssueDate() == null) {
                errors.add("Dòng " + rowIndex + ": Ngày cấp không được để trống");
                continue;
            }

            ZonedDateTime issueDate;
            try {
                LocalDate localDate = LocalDate.parse(row.getIssueDate(), formatter);
                issueDate = localDate.atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh"));
                ZonedDateTime oneYearAgo = now.minusYears(1);
                ZonedDateTime oneYearLater = now.plusYears(1);

                if (issueDate.isBefore(oneYearAgo) || issueDate.isAfter(oneYearLater)) {
                    errors.add("Dòng " + rowIndex + ": Ngày cấp chứng chỉ phải trong vòng 1 năm trước và 1 năm sau kể từ hôm nay");
                    continue;
                }
            } catch (DateTimeParseException e) {
                errors.add("Dòng " + rowIndex + ": Ngày cấp chứng chỉ không đúng định dạng dd/MM/yyyy");
                continue;
            }

            if (row.getGrantor() == null || row.getGrantor().isBlank()) {
                errors.add("Dòng " + rowIndex + ": Chức vụ người cấp không được để trống");
                continue;
            }

            if (row.getSigner() == null || row.getSigner().isBlank()) {
                errors.add("Dòng " + rowIndex + ": Người ký không được để trống");
                continue;
            }

            if (row.getDiplomaNumber() == null || row.getDiplomaNumber().isBlank()) {
                errors.add("Dòng " + rowIndex + ": Số hiệu bằng không được để trống");
                continue;
            }

            Student student = studentMap.get(row.getStudentCode());
            if (student == null) {
                errors.add("Dòng " + rowIndex + ": Mã sinh viên không tồn tại trong khoa");
                continue;
            }

            String studentCode = row.getStudentCode();
            Boolean hasCertificate = existingCertificates.get(studentCode);
            if (Boolean.TRUE.equals(hasCertificate)) {
                errors.add("Dòng " + rowIndex + ": Sinh viên đã có loại chứng chỉ này");
                continue;
            }

            Certificate certificate = new Certificate();
            certificate.setStudent(student);
            certificate.setUniversityCertificateType(universityCertificateType);
            certificate.setIssueDate(issueDate.toLocalDate());
            certificate.setDiplomaNumber(row.getDiplomaNumber());
            certificate.setGrantor(row.getGrantor());
            certificate.setSigner(row.getSigner());
            certificate.setBlockchainTxHash(null);
            certificate.setQrCodeUrl(null);
            certificate.setStatus(Status.PENDING);
            certificate.setCreatedAt(now.toLocalDateTime());
            certificate.setUpdatedAt(now.toLocalDateTime());

            certificatesToSave.add(certificate);

            CertificatePrintData printData = new CertificatePrintData();
            printData.setUniversityName(universityCertificateType.getUniversity().getName());
            printData.setCertificateTitle("GIẤY CHỨNG NHẬN");
            printData.setStudentName(student.getName());
            printData.setDepartmentName("Khoa " + student.getStudentClass().getDepartment().getName());
            printData.setCertificateName(universityCertificateType.getCertificateType().getName());
            printData.setDiplomaNumber("Số: " + row.getDiplomaNumber());
            printData.setIssueDate("Ngày " + issueDate.getDayOfMonth() + " tháng " + issueDate.getMonthValue() + " năm " + issueDate.getYear());
            printData.setGrantor(row.getGrantor());
            printData.setSigner(row.getSigner());

            printDataList.add(printData);
        }

        if (!errors.isEmpty()) {
            throw new ListBadRequestException("Dữ liệu không hợp lệ", errors);
        }

        //tạo ảnh song song
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<String>> imageFutures = printDataList.stream()
                .map(printData -> executor.submit(() -> graphicsTextWriter.drawCertificateText(printData)))
                .collect(Collectors.toList());

        for (int i = 0; i < certificatesToSave.size(); i++) {
            try {
                certificatesToSave.get(i).setImageUrl(imageFutures.get(i).get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Lỗi tạo ảnh cho chứng chỉ", e);
            }
        }

        executor.shutdown();
        certificateService.saveAll(certificatesToSave);
    }

}
