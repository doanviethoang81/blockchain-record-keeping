package com.example.blockchain.record.keeping.services;


import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.models.User;
import com.example.blockchain.record.keeping.repositorys.UniversityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UniversityService implements IUniversityService{

    private final UniversityRepository universityRepository;

    @Override
    public University getUniversityByEmail(String email) {
        return universityRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User không tồn tại"));
    }

    @Override
    public List<User> findAllUserUniversity(String nameUniversity) {
        return universityRepository.findAllUserUniversity(nameUniversity);
    }

    @Override
    public University findById(Long id) {
        return universityRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy trường có id "+ id));
    }

    @Override
    public University save(University university) {
        return universityRepository.save(university);
    }
}
