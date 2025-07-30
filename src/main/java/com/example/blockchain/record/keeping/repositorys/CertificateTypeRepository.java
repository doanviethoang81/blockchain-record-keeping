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

    //all chứng chỉ của 1 trường và tìm chứng chỉ theo tên (pdt)
    @Query(value = """
    SELECT ct.* FROM certificate_types ct
    JOIN university_certificate_types uct ON ct.id = uct.certificate_type_id
    WHERE uct.university_id = :universityId
    AND (:name IS NULL OR LOWER(ct.name) LIKE LOWER(CONCAT('%', :name, '%')))
    and ct.status ='ACTIVE'
    ORDER BY ct.updated_at DESC    
    """, nativeQuery = true)
    List<CertificateType> searchByUniversityAndName(@Param("universityId") Long universityId,
                                                    @Param("name") String name);

    //kiem tra xem có loại ch ch nay da cap cho ai chưa (delete)
    @Query(value = """
        select count(c.university_certificate_type_id) as total from certificate_types ct
        join university_certificate_types uct on ct.id = uct.certificate_type_id
        join certificates c on uct.id = c.university_certificate_type_id
        where ct.id = :id
        and c.status = 'APPROVED'
        """,nativeQuery = true)
    long countCertificateType(@Param("id") Long id);
}
