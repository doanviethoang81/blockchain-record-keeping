package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.CertificateType;
import com.example.blockchain.record.keeping.models.University;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;


public interface ICertificateTypeService {

    Page<CertificateType> getAll(Pageable pageable);

    CertificateType createCertificateType(CertificateType certificateType);

    CertificateType findById(Long id);

    List<CertificateType> searchByName(String keyword);

    List<CertificateType> searchByUniversityAndName(Long universityId, String name);

    CertificateType update(Long id, String name);

    CertificateType delete(CertificateType certificateType);

    boolean existsByNameAndStatus(String name);

}
