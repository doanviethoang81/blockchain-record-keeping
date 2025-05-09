package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.CertificateType;
import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.models.UniversityCertificateType;

public interface IUniversityCertificateTypeService {

    UniversityCertificateType save(UniversityCertificateType universityCertificateType);

    UniversityCertificateType findById(Long id);

    UniversityCertificateType findByCartificateType(CertificateType id);

}
