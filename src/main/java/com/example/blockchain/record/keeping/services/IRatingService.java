package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.Rating;

public interface IRatingService {
    Rating findByName(String name);

}
