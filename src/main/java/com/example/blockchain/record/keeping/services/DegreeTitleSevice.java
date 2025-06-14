package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.Degree;
import com.example.blockchain.record.keeping.models.DegreeTitle;
import com.example.blockchain.record.keeping.repositorys.DegreeTitleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DegreeTitleSevice implements IDegreeTitleSevice {

    private final DegreeTitleRepository degreeTitleRepository;

    @Override
    public DegreeTitle findByName(String name) {
        return degreeTitleRepository.findByName(name)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy loại văn bằng!"));
    }

    @Override
    public DegreeTitle findById(Long id) {
        return degreeTitleRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy loại văn bằng với id= "+ id));
    }

    @Override
    public List<DegreeTitle> listDegree() {
        return degreeTitleRepository.findAll();
    }
}
