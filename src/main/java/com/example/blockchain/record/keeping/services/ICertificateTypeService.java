package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.CertificateType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface ICertificateTypeService {

    Page<CertificateType> getAll(Pageable pageable);

    CertificateType createCertificateType(CertificateType certificateType);
}
