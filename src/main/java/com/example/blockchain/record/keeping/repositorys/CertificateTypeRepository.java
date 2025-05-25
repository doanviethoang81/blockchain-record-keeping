package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.CertificateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateTypeRepository extends JpaRepository<CertificateType,Long> {
    Optional<CertificateType> findByIdAndStatus(Long id, Status status);

    boolean existsByNameAndStatus(String name, Status status);

    //tìm all chứng chỉ theo tên
    List<CertificateType> findTop10ByNameContainingIgnoreCase(String name);

    // tìm chứng chỉ theo pdt
    @Query("""
    SELECT ct FROM CertificateType ct
    JOIN UniversityCertificateType uct ON ct.id = uct.certificateType.id
    WHERE uct.university.id = :universityId AND LOWER(ct.name) LIKE LOWER(CONCAT('%', :name, '%'))
    """)
    List<CertificateType> searchByUniversityAndName(@Param("universityId") Long universityId, @Param("name") String name);

}
