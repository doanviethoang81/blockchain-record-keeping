package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.models.UniversityCertificateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UniversityCertificateTypeRepository extends JpaRepository<UniversityCertificateType,Long> {

}
