package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.request.RatingRequest;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.Rating;

import java.util.List;

public interface IRatingService {
    Rating findByName(String name);

    Rating findById(Long id);

    List<Rating> listRating();

    Rating update(Rating rating,String name);

    Rating delete(Rating rating);

    boolean findByNameAndStatus(String name, Status status);

    Rating add(RatingRequest request);

}
