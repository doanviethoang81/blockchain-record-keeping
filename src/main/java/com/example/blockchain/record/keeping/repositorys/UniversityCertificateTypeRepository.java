package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.models.Certificate;
import com.example.blockchain.record.keeping.models.CertificateType;
import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.models.UniversityCertificateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UniversityCertificateTypeRepository extends JpaRepository<UniversityCertificateType,Long> {

    Optional<UniversityCertificateType> findById(Long id);

//    Optional<University> findByUniversity(University university);
    Optional<UniversityCertificateType> findByCertificateType(CertificateType certificateType);

    List<UniversityCertificateType> findByUniversity(University university);

    boolean existsByUniversityAndCertificateType_NameIgnoreCase(University university, String name);

    @Query(value = """
            SELECT ct.* from certificate_types ct
            JOIN university_certificate_types uct on ct.id =uct.certificate_type_id
            JOIN universitys u on  uct.university_id = u.id
            where u.id = :universityId and ct.status='ACTIVE'
            ORDER BY ct.created_at DESC;
            """, nativeQuery = true)
    List<CertificateType> findAllByUniversityOrderByCreatedAtDesc(@Param("universityId") Long universityId);
}
