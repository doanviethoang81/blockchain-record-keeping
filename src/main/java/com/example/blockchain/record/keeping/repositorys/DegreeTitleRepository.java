package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.DegreeTitle;
import com.example.blockchain.record.keeping.models.EducationMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DegreeTitleRepository extends JpaRepository<DegreeTitle,Long> {

    Optional<DegreeTitle> findByName(String name);

    List<DegreeTitle> findByStatus(Status status);

    boolean existsByNameAndStatus(String name, Status status);
}
