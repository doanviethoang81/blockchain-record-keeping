package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.annotation.Auditable;
import com.example.blockchain.record.keeping.aspect.AuditingContext;
import com.example.blockchain.record.keeping.dtos.CertificateTypeDTO;
import com.example.blockchain.record.keeping.dtos.request.CertificateTypeRequest;
import com.example.blockchain.record.keeping.enums.ActionType;
import com.example.blockchain.record.keeping.enums.Entity;
import com.example.blockchain.record.keeping.enums.LogTemplate;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.ActionChangeRepository;
import com.example.blockchain.record.keeping.repositorys.CertificateTypeRepository;
import com.example.blockchain.record.keeping.repositorys.LogRepository;
import com.example.blockchain.record.keeping.response.ApiResponseBuilder;
import jakarta.servlet.http.HttpServletRequest;
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
    private final AuditLogService auditLogService;
    private final HttpServletRequest httpServletRequest;
    private final LogRepository logRepository;
    private final ActionChangeRepository actionChangeRepository;

    @Override
    public Page<CertificateType> getAll(Pageable pageable) {
        return certificateTypeRepository.findAll(pageable);
    }

    @Override
    @Auditable(action = ActionType.CREATED, entity = Entity.certificate_types )
    public CertificateType createCertificateType(CertificateType certificateType) {
        AuditingContext.setDescription("Tạo loại chứng chỉ tên: " + certificateType.getCreatedAt());
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
        CertificateType certificateOld = auditLogService.cloneCertificateType(certificateType);

        certificateType.setName(name);
        certificateType.setUpdatedAt(vietnamTime.toLocalDateTime());
        CertificateType certificateNew = certificateType;

        List<ActionChange> changes = auditLogService.compareObjects(null, certificateOld, certificateNew);
        String ipAdress = auditLogService.getClientIp(httpServletRequest);
        if (!changes.isEmpty()) {// nếu khác old mới lưu
            Log log = new Log();
            log.setUser(auditLogService.getCurrentUser());
            log.setActionType(ActionType.UPDATED);
            log.setEntityName(Entity.certificate_types);
            log.setEntityId(id);
            log.setDescription(LogTemplate.UPDATE_CERTIFICATE_TYPE.format(certificateType.getName()));
            log.setIpAddress(ipAdress);
            log.setCreatedAt(vietnamTime.toLocalDateTime());

            log = logRepository.save(log);

            for (ActionChange change : changes) {
                change.setLog(log);
            }
            actionChangeRepository.saveAll(changes);
        }

        return certificateTypeRepository.save(certificateType);
    }

    @Override
    @Auditable(action = ActionType.DELETED, entity = Entity.certificate_types )
    public CertificateType delete(CertificateType certificateType) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        certificateType.setStatus(Status.DELETED);
        certificateType.setUpdatedAt(vietnamTime.toLocalDateTime());
        AuditingContext.setDescription("Xóa loại chứng chỉ: " + certificateType.getName());
        return certificateTypeRepository.save(certificateType);
    }

    @Override
    public boolean existsByNameAndStatus(String name) {
        return certificateTypeRepository.existsByNameAndStatus(name, Status.ACTIVE);
    }
}
