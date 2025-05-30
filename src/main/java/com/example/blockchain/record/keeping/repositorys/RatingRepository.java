package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.models.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating,Long> {
    Optional<Rating> findByName(String name);
}
