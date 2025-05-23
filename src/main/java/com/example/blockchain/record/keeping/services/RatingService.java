package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.Rating;
import com.example.blockchain.record.keeping.repositorys.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RatingService implements IRatingService {

    private final RatingRepository ratingRepository;

    @Override
    public Rating findByName(String name) {
        return ratingRepository.findByName(name)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy loại xếp hạng này!"));
    }
}
