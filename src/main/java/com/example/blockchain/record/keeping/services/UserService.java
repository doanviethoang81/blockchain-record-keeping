package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.request.ChangePasswordDepartmentRequest;
import com.example.blockchain.record.keeping.dtos.request.ChangePasswordRequest;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.PermissionRepository;
import com.example.blockchain.record.keeping.repositorys.StudentRepository;
import com.example.blockchain.record.keeping.repositorys.UserPermissionRepository;
import com.example.blockchain.record.keeping.repositorys.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PermissionRepository permissionRepository;
    private final UserPermissionRepository userPermissionRepository;

    @Override
    public User findByUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy user!"));
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public List<User> listUser(University university) {
        return userRepository.findByUniversity(university);
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
                .orElseThrow(()-> new RuntimeException("Không tìm thấy khoa!"));
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
    public boolean changePasswordDepartment(ChangePasswordDepartmentRequest changePasswordDepartmentRequest){
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        Optional<User> userOptional = userRepository.findById(changePasswordDepartmentRequest.getId());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setPassword(changePasswordDepartmentRequest.getNewPassword());
            user.setUpdatedAt(vietnamTime.toLocalDateTime());
            userRepository.save(user);
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
            save(user);
            return false;
        } else {
            // neu chưa có quyền, thêm vào
            UserPermission newPermission = new UserPermission();
            newPermission.setUser(user);
            newPermission.setPermission(writePermission);
            userPermissionRepository.save(newPermission);

            user.setUpdatedAt(vietnamTime.toLocalDateTime());
            save(user);
            return true;
        }
    }


}
