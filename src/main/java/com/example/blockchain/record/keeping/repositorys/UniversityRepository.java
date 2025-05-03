package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.models.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UniversityRepository extends JpaRepository<University, Long> {
    Optional<University> findByEmail(String email);
}
