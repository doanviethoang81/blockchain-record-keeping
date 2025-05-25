package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.CertificateTypeDTO;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.CertificateType;
import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.repositorys.CertificateTypeRepository;
import com.example.blockchain.record.keeping.response.ApiResponseBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
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
        return certificateTypeRepository.findByIdAndStatus(id, Status.ACTIVE)
                .orElseThrow(()->new RuntimeException("Không tìm thấy id chứng chỉ: " + id));
    }

    // tìm chứng chỉ theo tên all
    @Override
    public List<CertificateType> searchByName(String keyword) {
        return certificateTypeRepository.findTop10ByNameContainingIgnoreCase(keyword);
    }

    // tìm tên chứng chỉ theo pdt
    @Override
    public List<CertificateType> searchByUniversityAndName(Long universityId, String name) {
        return certificateTypeRepository.searchByUniversityAndName(universityId, name);
    }

    @Override
    public CertificateType update(Long id, String name) {

        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        CertificateType certificateType = certificateTypeRepository.findByIdAndStatus(id, Status.ACTIVE)
                        .orElseThrow(()-> new RuntimeException("Không tìm thấy loại chứng chỉ có id "+ id));
        certificateType.setName(name);
        certificateType.setUpdatedAt(vietnamTime.toLocalDateTime());
        return certificateTypeRepository.save(certificateType);
    }

    @Override
    public CertificateType delete(CertificateType certificateType) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        certificateType.setStatus(Status.DELETED);
        certificateType.setUpdatedAt(vietnamTime.toLocalDateTime());
        return certificateTypeRepository.save(certificateType);
    }

    @Override
    public boolean existsByNameAndStatus(String name) {
        return certificateTypeRepository.existsByNameAndStatus(name, Status.ACTIVE);
    }

}
