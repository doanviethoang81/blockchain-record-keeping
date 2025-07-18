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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class DepartmentExcelListener extends AnalysisEventListener<DepartmentExcelRowDTO> {

    private final DepartmentService departmentService;
    private final AuditLogService auditLogService;
    private final LogRepository logRepository;
    private final HttpServletRequest httpServletRequest;
    private final University university;
    private final UserService userService;
    private final UserPermissionService userPermissionService;
    private final PasswordEncoder passwordEncoder;
    private final PermissionService permissionService;
    private final RoleService roleService;

    public DepartmentExcelListener(DepartmentService departmentService, AuditLogService auditLogService, LogRepository logRepository, HttpServletRequest httpServletRequest, University university, UserService userService, UserPermissionService userPermissionService, PasswordEncoder passwordEncoder, PermissionService permissionService, RoleService roleService) {
        this.departmentService = departmentService;
        this.auditLogService = auditLogService;
        this.logRepository = logRepository;
        this.httpServletRequest = httpServletRequest;
        this.university = university;
        this.userService = userService;
        this.userPermissionService = userPermissionService;
        this.passwordEncoder = passwordEncoder;
        this.permissionService = permissionService;
        this.roleService = roleService;
    }

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

        //thu thập tên khoa từ file
        Set<String> fileDepartmentNames = rows.stream()
                .map(DepartmentExcelRowDTO::getName)
                .filter(Objects::nonNull)
                .map(String::trim)
                .collect(Collectors.toSet());

        //lấy danh sách khoa đã tồn tại trong DB theo tên và universityId
        Set<String> existingDepartmentNames = departmentService
                .findByUniversityIdAndNames(university.getId(), fileDepartmentNames)
                .stream()
                .map(d -> d.getName().trim())
                .collect(Collectors.toSet());

        //lấy danh sách email từ file
        Set<String> fileEmails = rows.stream()
                .map(DepartmentExcelRowDTO::getEmail)
                .filter(Objects::nonNull)
                .map(String::trim)
                .collect(Collectors.toSet());
        //lấy email bị trùng từ db
        Set<String> existingEmails = userService.findEmailsByUniversityIdAndEmails(university.getId(), fileEmails)
                .stream()
                .map(String::trim)
                .collect(Collectors.toSet());

        for (int i = 0; i < rows.size(); i++) {
            DepartmentExcelRowDTO row = rows.get(i);
            int rowIndex = i + 1;

            if (row.getName() == null || row.getName().isBlank()) {
                errors.add("Dòng " + rowIndex + ": Tên khoa không được để trống");
                continue;
            }
            if (row.getEmail() == null || row.getEmail().isBlank()) {
                errors.add("Dòng " + rowIndex + ": Email khoa không được để trống");
                continue;
            }
            if (row.getPassword() == null || row.getPassword().isBlank()) {
                errors.add("Dòng " + rowIndex + ": Password không được để trống");
                continue;
            }

            if (existingDepartmentNames.contains(row.getName().trim())) {
                errors.add("Dòng " + rowIndex + ": Tên khoa '" + row.getName() + "' đã tồn tại");
            }
            if (existingEmails.contains(row.getEmail().trim())) {
                errors.add("Dòng " + rowIndex + ": Email '" + row.getEmail() + "' đã tồn tại");
            }
        }

        if (!errors.isEmpty()) {
            throw new ListBadRequestException("Dữ liệu không hợp lệ", errors);
        }

        Role departmentRole = roleService.findByName("KHOA");
        List<Permission> allPermissions = permissionService.listPermission();

        for (DepartmentExcelRowDTO row : rows) {
            Department department = new Department();
            department.setName(row.getName().trim());
            department.setUniversity(university);
            department.setStatus(Status.ACTIVE);
            department.setCreatedAt(now.toLocalDateTime());
            department.setUpdatedAt(now.toLocalDateTime());
            departmentService.save(department);

            User user = new User();
            user.setUniversity(university);
            user.setEmail(row.getEmail().trim());
            user.setPassword(passwordEncoder.encode(row.getPassword().trim()));
            user.setRole(departmentRole);
            user.setDepartment(department);
            user.setVerified(true);
            user.setCreatedAt(now.toLocalDateTime());
            user.setUpdatedAt(now.toLocalDateTime());
            userService.save(user);

            for (Permission permission : allPermissions) {
                UserPermission userPermission = new UserPermission();
                userPermission.setUser(user);
                userPermission.setPermission(permission);
                userPermissionService.save(userPermission);
            }

            String ipAdress = auditLogService.getClientIp(httpServletRequest);
            Log log = new Log();
            log.setUser(auditLogService.getCurrentUser());
            log.setActionType(ActionType.CREATED);
            log.setEntityName(Entity.departments);
            log.setEntityId(department.getId());
            log.setDescription(LogTemplate.CREATE_DEPARTMENT_EXCEL.format(department.getName()));
            log.setCreatedAt(now.toLocalDateTime());
            log.setIpAddress(ipAdress);
            logRepository.save(log);
        }
    }

    @Override
    public boolean hasNext(AnalysisContext context) {
        return super.hasNext(context);
    }
}
