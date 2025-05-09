package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.CertificateTypeDTO;
import com.example.blockchain.record.keeping.models.CertificateType;
import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.repositorys.CertificateTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CertificateTypeService implements ICertificateTypeService{

    private final CertificateTypeRepository certificateTypeRepository;

    @Override
    public Page<CertificateType> getAll(Pageable pageable) {
        return certificateTypeRepository.findAll(pageable);
    }

    @Override
    public CertificateType createCertificateType(CertificateType certificateType) {
        return certificateTypeRepository.save(certificateType);
    }

    @Override
    public CertificateType findById(Long id) {
        return certificateTypeRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Không tìm thấy id chứng chỉ: " + id));
    }
}
