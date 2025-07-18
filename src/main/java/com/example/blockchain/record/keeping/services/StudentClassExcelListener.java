package com.example.blockchain.record.keeping.services;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.example.blockchain.record.keeping.dtos.DepartmentExcelRowDTO;
import com.example.blockchain.record.keeping.dtos.StudentClassExcelRowDTO;
import com.example.blockchain.record.keeping.enums.ActionType;
import com.example.blockchain.record.keeping.enums.Entity;
import com.example.blockchain.record.keeping.enums.LogTemplate;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.exceptions.ListBadRequestException;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.LogRepository;
import jakarta.servlet.http.HttpServletRequest;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class StudentClassExcelListener extends AnalysisEventListener<StudentClassExcelRowDTO> {

    private final StudentClassService studentClassService;
    private final HttpServletRequest httpServletRequest;
    private final AuditLogService auditLogService;
    private final LogRepository logRepository;
    private final Department department;

    private final List<StudentClassExcelRowDTO> rows = new ArrayList<>();

    public StudentClassExcelListener(StudentClassService studentClassService, HttpServletRequest httpServletRequest, AuditLogService auditLogService, LogRepository logRepository, Department department) {
        this.studentClassService = studentClassService;
        this.httpServletRequest = httpServletRequest;
        this.auditLogService = auditLogService;
        this.logRepository = logRepository;
        this.department = department;
    }

    @Override
    public void invoke(StudentClassExcelRowDTO data, AnalysisContext analysisContext) {
        rows.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        List<String> errors = new ArrayList<>();

        //thu thập tên lớp từ file
        Set<String> fileStudentClassNames = rows.stream()
                .map(StudentClassExcelRowDTO::getName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .collect(Collectors.toSet());

        //lấy danh sách lớp đã tồn tại trong DB theo tên và departmentId
        Set<String> existingDepartmentNames = studentClassService
                .findByUniversityIdAndNames(department.getUniversity().getId(), fileStudentClassNames)
                .stream()
                .map(d -> d.getName().trim())
                .collect(Collectors.toSet());


        for (int i = 0; i < rows.size(); i++) {
            StudentClassExcelRowDTO row = rows.get(i);
            int rowIndex = i + 1;

            if (row.getName() == null || row.getName().isBlank()) {
                errors.add("Dòng " + rowIndex + ": Tên khoa không được để trống");
                continue;
            }

            if (existingDepartmentNames.contains(row.getName().trim())) {
                errors.add("Dòng " + rowIndex + ": Tên lớp '" + row.getName() + "' đã tồn tại trong trường");
            }

        }

        if (!errors.isEmpty()) {
            throw new ListBadRequestException("Dữ liệu không hợp lệ", errors);
        }

        for (StudentClassExcelRowDTO row : rows) {
            StudentClass studentClass= new StudentClass();
            studentClass.setDepartment(department);
            studentClass.setName(row.getName());
            studentClass.setStatus(Status.ACTIVE);
            studentClass.setCreatedAt(now.toLocalDateTime());
            studentClass.setUpdatedAt(now.toLocalDateTime());
            studentClassService.save(studentClass);

            String ipAdress = auditLogService.getClientIp(httpServletRequest);
            Log log = new Log();
            log.setUser(auditLogService.getCurrentUser());
            log.setActionType(ActionType.CREATED);
            log.setEntityName(Entity.student_class);
            log.setEntityId(studentClass.getId());
            log.setDescription(LogTemplate.CREATE_CLASS_EXCEL.format(studentClass.getName()));
            log.setCreatedAt(now.toLocalDateTime());
            log.setIpAddress(ipAdress);
            logRepository.save(log);
        }
    }
}
