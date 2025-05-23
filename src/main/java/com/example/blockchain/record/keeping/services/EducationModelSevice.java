package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.EducationMode;
import com.example.blockchain.record.keeping.repositorys.EducationModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EducationModelSevice implements IEducationModelSevice {
    private final EducationModelRepository educationModelRepository;

    @Override
    public EducationMode findByName(String name) {
        return educationModelRepository.findByName(name)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy hình thức đào tạo!"));
    }
}
