package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.CertificateType;
import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.models.UniversityCertificateType;

import java.util.List;

public interface IUniversityCertificateTypeService {

    UniversityCertificateType save(UniversityCertificateType universityCertificateType);

    UniversityCertificateType findById(Long id);

    UniversityCertificateType findByCartificateType(CertificateType id);

    List<UniversityCertificateType> listUniversityCertificateType(University university);


}
