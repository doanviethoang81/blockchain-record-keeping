package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.DegreeTitle;
import com.example.blockchain.record.keeping.repositorys.DegreeTitleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DegreeTitleSevice implements IDegreeTitleSevice {

    private final DegreeTitleRepository degreeTitleRepository;

    @Override
    public DegreeTitle findByName(String name) {
        return degreeTitleRepository.findByName(name)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy loại văn bằng!"));
    }
}
