package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.EducationMode;
import com.example.blockchain.record.keeping.repositorys.EducationModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EducationModelSevice implements IEducationModelSevice {
    private final EducationModelRepository educationModelRepository;

    @Override
    public EducationMode findByName(String name) {
        return educationModelRepository.findByName(name)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy hình thức đào tạo!"));
    }

    @Override
    public EducationMode findById(Long id) {
        return educationModelRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy hình thức đào tạo với id= "+ id));
    }

    @Override
    public List<EducationMode> listEducationMode() {
        return educationModelRepository.findAll();
    }
}
