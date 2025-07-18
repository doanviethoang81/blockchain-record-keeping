package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.CertificateType;
import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.models.UniversityCertificateType;
import com.example.blockchain.record.keeping.repositorys.UniversityCertificateTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UniversityCertificateTypeService implements IUniversityCertificateTypeService{

    private final UniversityCertificateTypeRepository universityCertificateTypeRepository;

    @Override
    public UniversityCertificateType save(UniversityCertificateType universityCertificateType) {
        return universityCertificateTypeRepository.save(universityCertificateType);
    }

    @Override
    public UniversityCertificateType findById(Long id) {
        return universityCertificateTypeRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy id " + id));
    }

    @Override
    public UniversityCertificateType findByCartificateType(CertificateType certificateType) {
        return universityCertificateTypeRepository.findByCertificateType(certificateType)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy loại chứng chỉ! "));
    }

    @Override
    public List<UniversityCertificateType> listUniversityCertificateType(University university) {
        return universityCertificateTypeRepository.findByUniversity(university);
    }


    public boolean existsByUniversityAndCertificateName(University university, String certificateName) {
        return universityCertificateTypeRepository.existsByUniversityAndCertificateType_NameIgnoreCaseAndCertificateType_Status(university, certificateName.trim(), Status.ACTIVE);
    }

    public List<CertificateType> listUniversityCertificateTypes(Long universityId) {
        return universityCertificateTypeRepository.findAllByUniversityOrderByCreatedAtDesc(universityId);
    }

}
