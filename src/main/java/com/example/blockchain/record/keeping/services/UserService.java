package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.request.ChangePasswordDepartmentRequest;
import com.example.blockchain.record.keeping.dtos.request.ChangePasswordRequest;
import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.Student;
import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.models.User;
import com.example.blockchain.record.keeping.repositorys.StudentRepository;
import com.example.blockchain.record.keeping.repositorys.UserRepository;
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

}
