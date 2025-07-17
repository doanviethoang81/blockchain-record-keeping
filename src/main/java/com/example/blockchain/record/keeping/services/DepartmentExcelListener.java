package com.example.blockchain.record.keeping.services;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.CellExtra;
import com.example.blockchain.record.keeping.dtos.DepartmentExcelRowDTO;
import com.example.blockchain.record.keeping.enums.*;
import com.example.blockchain.record.keeping.exceptions.ListBadRequestException;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.LogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DepartmentExcelListener extends AnalysisEventListener<DepartmentExcelRowDTO> {

    private final DepartmentService departmentService;
    private final AuditLogService auditLogService;
    private final LogRepository logRepository;
    private final HttpServletRequest httpServletRequest;
    private final University university;

    @Override
    public void onException(Exception exception, AnalysisContext context) throws Exception {
        super.onException(exception, context);
    }
    private final List<DepartmentExcelRowDTO> rows = new ArrayList<>();

    @Override
    public void invoke(DepartmentExcelRowDTO data, AnalysisContext analysisContext) {
        rows.add(data);
    }

    @Override
    public void extra(CellExtra extra, AnalysisContext context) {
        super.extra(extra, context);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        List<String> errors = new ArrayList<>();

        // B1: Thu thập tên khoa từ file
        Set<String> fileDepartmentNames = rows.stream()
                .map(DepartmentExcelRowDTO::getName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .collect(Collectors.toSet());

        // B2: Lấy danh sách khoa đã tồn tại trong DB theo tên và universityId
        Set<String> existingDepartmentNames = departmentService
                .findByUniversityIdAndNames(university.getId(), fileDepartmentNames)
                .stream()
                .map(d -> d.getName().trim())
                .collect(Collectors.toSet());

        // B3: So sánh - nếu tên khoa đã tồn tại thì báo lỗi
        for (int i = 0; i < rows.size(); i++) {
            DepartmentExcelRowDTO row = rows.get(i);
            int rowIndex = i + 1;

            if (row.getName() == null || row.getName().isBlank()) {
                errors.add("Dòng " + rowIndex + ": Tên khoa không được để trống");
                continue;
            }

            if (existingDepartmentNames.contains(row.getName().trim())) {
                errors.add("Dòng " + rowIndex + ": Tên khoa '" + row.getName() + "' đã tồn tại");
            }
        }

        if (!errors.isEmpty()) {
            throw new ListBadRequestException("Dữ liệu không hợp lệ", errors);
        }

        // B4: Lưu các khoa mới
        List<Department> departmentsToSave = rows.stream()
                .map(row -> {
                    Department department = new Department();
                    department.setName(row.getName().trim());
                    department.setUniversity(university); // giả sử có constructor Long id
                    department.setStatus(Status.ACTIVE);
                    department.setCreatedAt(now.toLocalDateTime());
                    department.setUpdatedAt(now.toLocalDateTime());
                    return department;
                })
                .collect(Collectors.toList());

        departmentService.saveAll(departmentsToSave);

        // B5: Ghi log
        String ipAddress = auditLogService.getClientIp(httpServletRequest);
        Log log = new Log();
        log.setUser(auditLogService.getCurrentUser());
        log.setActionType(ActionType.CREATED);
        log.setEntityName(Entity.departments);
        log.setEntityId(null);
        log.setDescription("Import " + departmentsToSave.size() + " khoa mới cho trường ID " + university);
        log.setIpAddress(ipAddress);
        log.setCreatedAt(now.toLocalDateTime());
        logRepository.save(log);
    }


    @Override
    public boolean hasNext(AnalysisContext context) {
        return super.hasNext(context);
    }
}
