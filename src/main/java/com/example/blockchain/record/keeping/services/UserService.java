package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.annotation.Auditable;
import com.example.blockchain.record.keeping.dtos.StatisticsAdminDTO;
import com.example.blockchain.record.keeping.dtos.StatisticsDepartmentDTO;
import com.example.blockchain.record.keeping.dtos.StatisticsUniversityDTO;
import com.example.blockchain.record.keeping.dtos.request.ChangePasswordDepartmentRequest;
import com.example.blockchain.record.keeping.dtos.request.ChangePasswordRequest;
import com.example.blockchain.record.keeping.enums.ActionType;
import com.example.blockchain.record.keeping.enums.Entity;
import com.example.blockchain.record.keeping.enums.LogTemplate;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.exceptions.BadRequestException;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.*;
import com.example.blockchain.record.keeping.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PermissionRepository permissionRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditLogService auditLogService;
    private final HttpServletRequest httpServletRequest;
    private final LogRepository logRepository;
    private final UniversityService universityService;

    @Override
    public User findByUser(String email) {
        return userRepository.findByEmail(email)
                .orElse(null   );
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> listDepartmentOfUniversity(Long universityId, String name) {
        return userRepository.findUserDepartmentByUniversity(universityId,name);
    }

    @Override
    public boolean isEmailRegistered(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    @Override
    public boolean updateLocked(Long userId) {
        try {
            ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

            Optional<User> optionalUser = userRepository.findById(userId);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                user.setLocked(!user.isLocked());
                user.setUpdatedAt(vietnamTime.toLocalDateTime());
                userRepository.save(user);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public User finbById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(()-> new BadRequestException("Không tìm thấy user có id: "+ id));
    }


    public boolean changePassword(String email, ChangePasswordRequest changePasswordRequest) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {

                if (changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmPassword())) {
                    user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
                    user.setUpdatedAt(vietnamTime.toLocalDateTime());
                    userRepository.save(user);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean changePasswordDepartment(Long id, ChangePasswordDepartmentRequest changePasswordDepartmentRequest){
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        Optional<User> userOptional = userRepository.findById(id);
        Department department = departmentRepository.findById(userOptional.get().getDepartment().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khoa") );

//        User userOld = auditLogService.cloneUser(userOptional);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setPassword(passwordEncoder.encode(changePasswordDepartmentRequest.getNewPassword()));
            user.setUpdatedAt(vietnamTime.toLocalDateTime());
            userRepository.save(user);

            department.setUpdatedAt(vietnamTime.toLocalDateTime());
            departmentRepository.save(department);

            String ipAdress = auditLogService.getClientIp(httpServletRequest);
            Log log = new Log();
            log.setUser(auditLogService.getCurrentUser());
            log.setActionType(ActionType.CHANGE_PASSWORD_DEPARTMENT);
            log.setEntityName(Entity.departments);
            log.setEntityId(department.getId());
            log.setDescription(LogTemplate.CHANGE_PASSWORD_DEPARTMENT.format(department.getName()));
            log.setIpAddress(ipAdress);
            log.setCreatedAt(vietnamTime.toLocalDateTime());
            log = logRepository.save(log);

            User userNew = user;

//            List<ActionChange> changes = auditLogService.compareObjects(log, userOld, userNew);
//            if (!changes.isEmpty()) {
//                actionChangeRepository.saveAll(changes);
//                logRepository.save(log);
//            }
            return true;
        }
        return false;

    }

    @Transactional
    @Override
    public boolean togglePermission(Long userId,String action) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        Permission writePermission = permissionRepository.findByAction(action)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy quyền "+ action));

        Optional<UserPermission> userPermission = userPermissionRepository
                .findByUserIdAndPermissionId(user.getId(), writePermission.getId());

        if (userPermission.isPresent()) {
            //nếu đã có quyền, thu hồi
            userPermissionRepository.delete(userPermission.get());
            user.setUpdatedAt(vietnamTime.toLocalDateTime());
            userRepository.save(user);
            return false;
        } else {
            // neu chưa có quyền, thêm vào
            UserPermission newPermission = new UserPermission();
            newPermission.setUser(user);
            newPermission.setPermission(writePermission);
            userPermissionRepository.save(newPermission);

            user.setUpdatedAt(vietnamTime.toLocalDateTime());
            userRepository.save(user);
            return true;
        }
    }

    @Override
    public User findByUniversity(University university) {
        return userRepository.findByUniversity(university);
    }

    @Override
    public StatisticsAdminDTO dashboardAdmin() {
        return  userRepository.getStatisticsAdmin();
    }

    @Override
    public StatisticsUniversityDTO dashboardUniversity(Long universityId) {
        return userRepository.getStatisticsUniversity(universityId);
    }

    @Override
    public StatisticsDepartmentDTO dashboarDepartment(Long departmentId) {
        return userRepository.getStatisticsDepartment(departmentId);
    }

    @Override
    public User findByDepartment(Department department) {
        return userRepository.findByDepartment(department)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy khoa !"));
    }

    @Override
    public boolean resetPassword(String email, String newPassword) {
        User user= userRepository.findByEmail(email)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy tài khoản này!"));
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(vietnamTime.toLocalDateTime());
        userRepository.save(user);
        return true;
    }

    @Override
    public Set<String> findEmailsByUniversityIdAndEmails(Long universityId, Set<String> emails) {
            return userRepository.findEmailsByUniversityIdAndEmails(universityId, emails);
    }

    // danh sách user khoa thuộc 1 tr
    public List<UserResponse> getDepartmentDetailOfUniversity(Long id, String name) {

        List<User> listUser = userRepository.findUserDepartmentByUniversity(id, name);
        List<UserResponse> userRepons = new ArrayList<>();

        for (User user : listUser) {
            List<UserPermission> userPermissions = userPermissionRepository.findByUser(user);

            List<String> permissions = Optional.ofNullable(userPermissionRepository.findByUser(user))
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(up -> up.getPermission().getAction())
                    .collect(Collectors.toList());


            UserResponse userResponse = new UserResponse(
                    user.getId(),
                    user.getDepartment().getName(),
                    user.getEmail(),
                    user.isLocked(),
                    permissions
            );
            userRepons.add(userResponse);
        }
        return userRepons;
    }
}
