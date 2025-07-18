package com.example.blockchain.record.keeping.services;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.CellExtra;
import com.example.blockchain.record.keeping.dtos.request.StudentExcelRowRequest;
import com.example.blockchain.record.keeping.enums.ActionType;
import com.example.blockchain.record.keeping.enums.Entity;
import com.example.blockchain.record.keeping.enums.LogTemplate;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.exceptions.BadRequestException;
import com.example.blockchain.record.keeping.exceptions.ListBadRequestException;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.ActionChangeRepository;
import com.example.blockchain.record.keeping.repositorys.LogRepository;
import com.example.blockchain.record.keeping.repositorys.StudentRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@RequiredArgsConstructor
public class StudentExcelListener extends AnalysisEventListener<StudentExcelRowRequest> {

    private final StudentRepository studentRepository;
    private final DepartmentService departmentService;
    private final UniversityService universityService;
    private final StudentClassService studentClassService;
    private final StudentService studentService;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final HttpServletRequest httpServletRequest;
    private final LogRepository logRepository;
    private final STUcoinService stUcoinService;
    private final WalletService walletService;

    private final List<StudentExcelRowRequest> rows = new ArrayList<>();
    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        super.onException(exception, context);
    }

    @Override
    public void invoke(StudentExcelRowRequest data, AnalysisContext context) {
        rows.add(data);
    }

    @Override
    public void extra(CellExtra extra, AnalysisContext context) {
        super.extra(extra, context);
    }

    @Override
    @Transactional
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        University university = universityService.getUniversityByEmail(username);
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        List<String> errors = new ArrayList<>();

        if (rows.isEmpty()) {
            throw new ListBadRequestException("File Excel không chứa dữ liệu", List.of("Không có dòng nào trong file"));
        }

        if (rows.size() > 1000) {
            throw new BadRequestException("Chỉ cho phép tối đa 1000 sinh viên/lần import");
        }

        Set<String> duplicateStudentCodes = new HashSet<>();
        Set<String> duplicateEmails = new HashSet<>();

        // Cache departments và student classes
        Map<String, Department> departmentCache = new HashMap<>();
        Map<String, StudentClass> classCache = new HashMap<>();

        List<Department> departments = departmentService.listDepartmentOfUniversity(university);
        for (Department dept : departments) {
            departmentCache.put(dept.getName(), dept);

            List<StudentClass> classes = studentClassService.findAllClassesByDepartmentId(dept.getId(), null);
            for (StudentClass cls : classes) {
                classCache.put(cls.getName() + "_" + dept.getId(), cls);
            }
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (int i = 0; i < rows.size(); i++) {
            StudentExcelRowRequest row = rows.get(i);
            int rowIndex = i + 1;

            if (!duplicateStudentCodes.add(row.getStudentCode())) {
                errors.add("Dòng " + rowIndex + ": Trùng mã sinh viên trong file");
            }

            if (!duplicateEmails.add(row.getEmail())) {
                errors.add("Dòng " + rowIndex + ": Trùng email trong file");
            }

            if (row.getName() == null || row.getName().isBlank()) {
                errors.add("Dòng " + rowIndex + ": Tên sinh viên không được để trống");
            } else if (!row.getName().matches("^[\\p{L}\\s]+$")) {
                errors.add("Dòng " + rowIndex + ": Tên sinh viên không được chứa số/ký tự đặc biệt");
            }

            if (row.getStudentCode() == null || row.getStudentCode().isBlank()) {
                errors.add("Dòng " + rowIndex + ": Mã số sinh viên không được để trống");
            }

            if (row.getEmail() == null || !row.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                errors.add("Dòng " + rowIndex + ": Email không đúng định dạng");
            }

            if (row.getDateOfBirth() == null) {
                errors.add("Dòng " + rowIndex + ": Ngày sinh không được để trống");
            } else {
                try {
                    LocalDate dateOfBirth = LocalDate.parse(row.getDateOfBirth(), formatter);
                    if (dateOfBirth.isAfter(LocalDate.now().minusYears(18))) {
                        errors.add("Dòng " + rowIndex + ": Ngày sinh dưới 18 tuổi");
                    }
                } catch (DateTimeParseException e) {
                    errors.add("Dòng " + rowIndex + ": Ngày sinh không đúng định dạng dd/MM/yyyy");
                }
            }

            if (row.getCourse() == null || row.getCourse().isBlank()) {
                errors.add("Dòng " + rowIndex + ": Khóa học không được để trống");
            }

            if (row.getDepartmentName() == null || row.getDepartmentName().isBlank()) {
                errors.add("Dòng " + rowIndex + ": Tên khoa không được để trống");
                continue;
            }

            if (row.getClassName() == null || row.getClassName().isBlank()) {
                errors.add("Dòng " + rowIndex + ": Tên lớp không được để trống");
                continue;
            }

            Department department = departmentCache.get(row.getDepartmentName());
            if (department == null) {
                errors.add("Dòng " + rowIndex + ": Không tìm thấy khoa '" + row.getDepartmentName() + "'");
                continue;
            }

            StudentClass studentClass = classCache.get(row.getClassName() + "_" + department.getId());
            if (studentClass == null) {
                errors.add("Dòng " + rowIndex + ": Lớp '" + row.getClassName() + "' không thuộc khoa '" + row.getDepartmentName() + "'");
                continue;
            }

            if (studentService.findByStudentCodeOfUniversity(row.getStudentCode(), university.getId()) != null) {
                errors.add("Dòng " + rowIndex + ": Mã sinh viên đã tồn tại trong hệ thống");
            }

            if (studentService.findByEmailStudentCodeOfDepartment(row.getEmail(), department.getUniversity().getId()) != null) {
                errors.add("Dòng " + rowIndex + ": Email sinh viên đã tồn tại trong trường");
            }
        }

        if (!errors.isEmpty()) {
            throw new ListBadRequestException("Dữ liệu không hợp lệ", errors);
        }

        for (StudentExcelRowRequest row : rows) {
            Department department = departmentCache.get(row.getDepartmentName());
            StudentClass studentClass = classCache.get(row.getClassName() + "_" + department.getId());

            LocalDate dateOfBirth = LocalDate.parse(row.getDateOfBirth(), formatter);
            Student student = new Student();
            student.setName(row.getName());
            student.setStudentCode(row.getStudentCode());
            student.setEmail(row.getEmail());
            student.setBirthDate(dateOfBirth);
            student.setCourse(row.getCourse());
            student.setStatus(Status.ACTIVE);
            student.setPassword(passwordEncoder.encode(row.getStudentCode()));
            student.setStudentClass(studentClass);
            student.setCreatedAt(now.toLocalDateTime());
            student.setUpdatedAt(now.toLocalDateTime());
            studentRepository.save(student);

            ECKeyPair ecKeyPair = null;
            try {
                ecKeyPair = Keys.createEcKeyPair();
            } catch (InvalidAlgorithmParameterException e) {
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (NoSuchProviderException e) {
                throw new RuntimeException(e);
            }
            String privateKey = ecKeyPair.getPrivateKey().toString(16);
            String publicKey = ecKeyPair.getPublicKey().toString(16);
            String walletAddress = "0x" + Keys.getAddress(ecKeyPair.getPublicKey());

            Wallet wallet = new Wallet();
            wallet.setStudent(student);
            wallet.setWalletAddress(walletAddress);
            wallet.setPrivateKey(privateKey);
            wallet.setPublicKey(publicKey);
            walletService.create(wallet);
            //gửi token
            try {
                stUcoinService.transferToStudent(wallet.getWalletAddress(), new BigInteger("5").multiply(BigInteger.TEN.pow(18))); // 5 STUcoin (18 decimals)
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        //log
        String ipAdress = auditLogService.getClientIp(httpServletRequest);
        Log log = new Log();
        log.setUser(auditLogService.getCurrentUser());
        log.setActionType(ActionType.CREATED);
        log.setEntityName(Entity.students);
        log.setEntityId(null);
        log.setDescription(LogTemplate.IMPORT_STUDENT.format(rows.size()));
        log.setIpAddress(ipAdress);
        log.setCreatedAt(now.toLocalDateTime());
        logRepository.save(log);
    }

    @Override
    public boolean hasNext(AnalysisContext context) {
        return super.hasNext(context);
    }
}
