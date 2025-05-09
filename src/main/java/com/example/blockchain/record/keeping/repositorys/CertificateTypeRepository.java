package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.models.CertificateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CertificateTypeRepository extends JpaRepository<CertificateType,Long> {
    Optional<CertificateType> findById(Long id);
}
