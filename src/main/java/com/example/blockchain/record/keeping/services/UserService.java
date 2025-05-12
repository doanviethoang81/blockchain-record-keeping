package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.Student;
import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.models.User;
import com.example.blockchain.record.keeping.repositorys.StudentRepository;
import com.example.blockchain.record.keeping.repositorys.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService{

    private final UserRepository userRepository;
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


}
