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

        for (int i = 0; i < rows.size(); i++) {
            CertificateExcelRowDTO row = rows.get(i);
            int rowIndex = i + 1;

            if (rows.size() > 500) {
                throw new BadRequestException("Chỉ cho phép tối đa 500 sinh viên/lần import");
            }

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
                errors.add("Dòng " + rowIndex + ": Ngày sinh không được để trống");
                continue;
            }

            try {
                LocalDate localDate = LocalDate.parse(row.getIssueDate(), formatter);
                ZonedDateTime issueDate = localDate.atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh"));
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
            Optional<Student> student = studentService.findByOneStudentOfDepartment(departmentId, row.getStudentCode());

            if (student.isEmpty()) {
                errors.add("Dòng " + rowIndex + ": Mã sinh viên này không tồn tại trong khoa");
                continue;
            }

            Optional<Certificate> certificate =
                    certificateService.existingStudentOfCertificate(
                            student.get().getId(),
                            universityCertificateType.getCertificateType().getId());
            if(!certificate.isEmpty()){
                errors.add("Dòng " + rowIndex + ": Sinh viên đã có loại chứng chỉ này");
                continue;
            }
        }

        if (!errors.isEmpty()) {
            throw new ListBadRequestException("Dữ liệu không hợp lệ", errors);
        }

        List<Certificate> certificatesToSave = new ArrayList<>();
        List<CertificatePrintData> printDataList = new ArrayList<>();

        for (CertificateExcelRowDTO row : rows) {
            Optional<Student> student = studentService.findByOneStudentOfDepartment(departmentId, row.getStudentCode());

            LocalDate dateOfBirth = LocalDate.parse(row.getIssueDate(), formatter);

            Certificate certificate = new Certificate();
            certificate.setStudent(student.get());
            certificate.setUniversityCertificateType(universityCertificateType);
            certificate.setIssueDate(dateOfBirth);
            certificate.setDiplomaNumber(row.getDiplomaNumber());
            certificate.setGrantor(row.getGrantor());
            certificate.setSigner(row.getSigner());
            certificate.setBlockchainTxHash(null);// fake


            certificate.setQrCodeUrl(null);// fake
            certificate.setStatus(Status.PENDING);
            certificate.setCreatedAt(now.toLocalDateTime());
            certificate.setUpdatedAt(now.toLocalDateTime());

            certificatesToSave.add(certificate);

            // tạo anh
            CertificatePrintData printData = new CertificatePrintData();
            printData.setUniversityName(universityCertificateType.getUniversity().getName());
            printData.setCertificateTitle("GIẤY CHỨNG NHẬN");
            printData.setStudentName(student.get().getName());
            printData.setDepartmentName("Khoa " + student.get().getStudentClass().getDepartment().getName());
            printData.setCertificateName(universityCertificateType.getCertificateType().getName());
            printData.setDiplomaNumber("Số: " + row.getDiplomaNumber());
            printData.setIssueDate("Ngày " + dateOfBirth.getDayOfMonth() + " tháng " + dateOfBirth.getMonthValue() + " năm " + dateOfBirth.getYear());
            printData.setGrantor(row.getGrantor());
            printData.setSigner(row.getSigner());

            printDataList.add(printData);
        }

        // tạo ảnh song song
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<Future<String>> imageFutures = new ArrayList<>();

        for (CertificatePrintData printData : printDataList) {
            Future<String> future = executor.submit(() -> graphicsTextWriter.drawCertificateText(printData));
            imageFutures.add(future);
        }

        for (int i = 0; i < certificatesToSave.size(); i++) {
            try {
                String imageUrl = imageFutures.get(i).get();
                certificatesToSave.get(i).setImageUrl(imageUrl);
                certificatesToSave.get(i).setQrCodeUrl("2347234234");
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Lỗi tạo ảnh cho chứng chỉ", e);
            }
        }

        executor.shutdown();

        certificateService.saveAll(certificatesToSave);
    }
}
