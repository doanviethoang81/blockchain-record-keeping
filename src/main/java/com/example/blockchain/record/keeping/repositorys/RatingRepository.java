package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.Rating;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating,Long> {
    Optional<Rating> findByName(String name);

    List<Rating> findByStatus(Status status);

    boolean existsByNameAndStatus(String name, Status status);
}
