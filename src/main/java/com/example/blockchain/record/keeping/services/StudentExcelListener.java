package com.example.blockchain.record.keeping.services;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.CellExtra;
import com.example.blockchain.record.keeping.dtos.request.StudentExcelRowRequest;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.exceptions.BadRequestException;
import com.example.blockchain.record.keeping.exceptions.ListBadRequestException;
import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.Student;
import com.example.blockchain.record.keeping.models.StudentClass;
import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.repositorys.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class StudentExcelListener extends AnalysisEventListener<StudentExcelRowRequest> {

    private final StudentRepository studentRepository;
    private final DepartmentService departmentService;
    private final UniversityService universityService;
    private final StudentClassService studentClassService;
    private final StudentService studentService;

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

        List<String> errors = new ArrayList<>();

        Set<String> duplicateStudentCodes = new HashSet<>();
        Set<String> duplicateEmails = new HashSet<>();

        for (int i = 0; i < rows.size(); i++) {
            StudentExcelRowRequest row = rows.get(i);
            int rowIndex = i + 1;

            if (rows.size() > 500) {
                throw new BadRequestException("Chỉ cho phép tối đa 500 sinh viên/lần import");
            }

            if (!duplicateStudentCodes.add(row.getStudentCode())) {
                errors.add("Dòng " + rowIndex + ": Trùng mã sinh viên trong file");
                continue;
            }

            if (!duplicateEmails.add(row.getEmail())) {
                errors.add("Dòng " + rowIndex + ": Trùng email trong file");
                continue;
            }

            if (row.getName() == null || row.getName().isBlank()) {
                errors.add("Dòng " + rowIndex + ": Tên sinh viên không được để trống");
                continue;
            }
            if (!row.getName().matches("^[\\p{L}\\s]+$")) {
                errors.add("Dòng " + rowIndex + ": Tên sinh viên không được chứa số/ký tự đặc biệt");
            }

            if (row.getStudentCode() == null || row.getStudentCode().isBlank()) {
                errors.add("Dòng " + rowIndex + ": Mã số sinh viên không được để trống");
                continue;
            }

            if (row.getEmail() == null || !row.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                errors.add("Dòng " + rowIndex + ": Email không đúng định dạng");
                continue;
            }

            if (row.getDateOfBirth() == null) {
                errors.add("Dòng " + rowIndex + ": Ngày sinh không được để trống");
                continue;
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            try {
                LocalDate dateOfBirth = LocalDate.parse(row.getDateOfBirth(), formatter);
                if (dateOfBirth.isAfter(LocalDate.now().minusYears(18))) {
                    errors.add("Dòng " + rowIndex + ": Ngày sinh dưới 18 tuổi");
                    continue;
                }
            } catch (DateTimeParseException e) {
                errors.add("Dòng " + rowIndex + ": Ngày sinh không đúng định dạng dd/MM/yyyy");
                continue;
            }

            if (row.getCourse() == null || row.getCourse().isBlank()) {
                errors.add("Dòng " + rowIndex + ": Khóa học không được để trống");
                continue;
            }

            if (row.getDepartmentName() == null || row.getDepartmentName().isBlank()) {
                errors.add("Dòng " + rowIndex + ": Tên khoa không được để trống");
                continue;
            }

            if (row.getClassName() == null || row.getClassName().isBlank()) {
                errors.add("Dòng " + rowIndex + ": Tên lớp không được để trống");
                continue;
            }

            if (studentService.findByStudentCodeOfUniversity(row.getStudentCode(), university.getId()) != null) {
                errors.add("Dòng " + rowIndex + ": Mã sinh viên đã tồn tại trong hệ thống");
                continue;
            }

            Department department = departmentService.findByDepartmentNameOfUniversity(university.getId(), row.getDepartmentName())
                    .orElse(null);
            if (department == null) {
                errors.add("Dòng " + rowIndex + ": Không tìm thấy khoa '" + row.getDepartmentName() + "'");
                continue;
            }

            if (studentService.findByEmailStudentCodeOfDepartment(row.getEmail(), department.getId()) != null) {
                errors.add("Dòng " + rowIndex + ": Email sinh viên đã tồn tại trong khoa");
                continue;
            }
        }

        if (!errors.isEmpty()) {
            throw new ListBadRequestException("Dữ liệu không hợp lệ", errors);
        }

        for (StudentExcelRowRequest row : rows) {
            Department department = departmentService.findByDepartmentNameOfUniversity(university.getId(), row.getDepartmentName())
                    .orElseThrow(() -> new BadRequestException("Không tìm thấy khoa: " + row.getDepartmentName()));

            StudentClass studentClass = studentClassService.findByClassNameAndDepartmentId(department.getId(), row.getClassName())
                    .orElseThrow(() -> new BadRequestException("Lớp '" + row.getClassName() + "' không thuộc khoa '" + row.getDepartmentName() + "'"));

            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate dateOfBirth = LocalDate.parse(row.getDateOfBirth(), formatter);

            Student student = new Student();
            student.setName(row.getName());
            student.setStudentCode(row.getStudentCode());
            student.setEmail(row.getEmail());
            student.setBirthDate(dateOfBirth);
            student.setCourse(row.getCourse());
            student.setStatus(Status.ACTIVE);
            student.setStudentClass(studentClass);
            student.setCreatedAt(now.toLocalDateTime());
            student.setUpdatedAt(now.toLocalDateTime());

            studentRepository.save(student);
        }
    }

    @Override
    public boolean hasNext(AnalysisContext context) {
        return super.hasNext(context);
    }
}
