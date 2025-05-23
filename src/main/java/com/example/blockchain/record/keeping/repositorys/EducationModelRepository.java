package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.models.EducationMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EducationModelRepository extends JpaRepository<EducationMode,Long> {
    Optional<EducationMode> findByName(String name);

}
