package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.User;
import com.example.blockchain.record.keeping.repositorys.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
