package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.annotation.Auditable;
import com.example.blockchain.record.keeping.dtos.request.DepartmentRequest;
import com.example.blockchain.record.keeping.dtos.request.UserDepartmentRequest;
import com.example.blockchain.record.keeping.enums.ActionType;
import com.example.blockchain.record.keeping.enums.Entity;
import com.example.blockchain.record.keeping.enums.LogTemplate;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepartmentService  implements IDepartmentService{

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserPermissionService userPermissionService;
    private final PermissionService permissionService;
    private final AuditLogService auditLogService;
    private final HttpServletRequest httpServletRequest;
    private final LogRepository logRepository;
    private final ActionChangeRepository actionChangeRepository;

    @Override
    public Department save(Department department) {
        return departmentRepository.save(department);
    }

    @Override
    public boolean existsByNameAndUniversity(String name, Long universityId) {
        return departmentRepository.existsByNameIgnoreCaseAndUniversityIdAndStatus(name.trim(), universityId, Status.ACTIVE);
    }

    @Override
    public boolean existsByNameIgnoreCaseAndUniversityIdAndStatusAndIdNot( String name, Long universityId, Long departmentId) {
        return departmentRepository.existsByNameIgnoreCaseAndUniversityIdAndStatusAndIdNot(name.trim(), universityId, Status.ACTIVE, departmentId);
    }

    @Override
    public List<Department> listDepartmentOfUniversity(University university) {
        return departmentRepository.findByUniversityAndStatus(university, Status.ACTIVE);
    }

    @Override
    public Department updateDepartment(Long id, DepartmentRequest request) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        Department department = departmentRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy khóa có id " + id));
        Department departmentOld = auditLogService.cloneDepartment(department);

        department.setName(request.getName());
        department.setUpdatedAt(vietnamTime.toLocalDateTime());
        User user = userRepository.findByDepartment(department)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy User với id "+ id));
        User userOld = auditLogService.cloneUser(user);
        user.setEmail(request.getEmail());
        user.setUpdatedAt(vietnamTime.toLocalDateTime());
        userRepository.save(user);
        departmentRepository.save(department);

        String ipAdress = auditLogService.getClientIp(httpServletRequest);
        List<ActionChange> changes = new ArrayList<>();

        changes.addAll(auditLogService.compareObjects(null, departmentOld, department));
        changes.addAll(auditLogService.compareObjects(null, userOld, user));

        if (!changes.isEmpty()) {
            Log log = new Log();
            log.setUser(auditLogService.getCurrentUser());
            log.setActionType(ActionType.UPDATED);
            log.setEntityName(Entity.departments);
            log.setEntityId(department.getId());
            log.setDescription(LogTemplate.UPDATE_DEPARTMENT.getName()+": "+ department.getName());
            log.setIpAddress(ipAdress);
            log.setCreatedAt(vietnamTime.toLocalDateTime());
            log = logRepository.save(log);

            for (ActionChange change : changes) {
                change.setLog(log);
            }
            actionChangeRepository.saveAll(changes);
        }
        return department;
    }


    @Override
    @Auditable(action = ActionType.DELETED, entity = Entity.departments)
    public Department deleteDepartment(Long id) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        Department departmentIsDelete = departmentRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy khóa có id " + id));
        departmentIsDelete.setStatus(Status.DELETED);
        departmentIsDelete.setUpdatedAt(vietnamTime.toLocalDateTime());

        User user = userRepository.findByDepartment(departmentIsDelete)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy thông tin khoa"));
        userRepository.delete(user);// xem lại nếu lỗi
        return departmentRepository.save(departmentIsDelete);
    }

    @Override
    public Department findById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy khoa có id "+ id));
    }

    @Override
    public Optional<Department> findByDepartmentNameOfUniversity(Long universityId, String name) {
        return departmentRepository.findByDepartmentNameOfUniversity(universityId,name);
    }

    @Override
    @Auditable(action = ActionType.CREATED, entity = Entity.departments)
    public Department create(UserDepartmentRequest request, University university) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        Role role =roleService.findByName("KHOA");

        Department department = new Department();
        department.setName(request.getName());
        department.setUniversity(university);
        department.setStatus(Status.ACTIVE);
        department.setCreatedAt(vietnamTime.toLocalDateTime());
        department.setUpdatedAt(vietnamTime.toLocalDateTime());
        departmentRepository.save(department);

        User user = new User();
        user.setUniversity(university);
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setDepartment(department);
        user.setCreatedAt(vietnamTime.toLocalDateTime());
        user.setUpdatedAt(vietnamTime.toLocalDateTime());

        userService.save(user);

        List<Permission> allPermissions = permissionService.listPermission();
        for (Permission permission : allPermissions) {
            UserPermission userPermission = new UserPermission();
            userPermission.setUser(user);
            userPermission.setPermission(permission);
            userPermissionService.save(userPermission);
        }
        return department;
    }
}
