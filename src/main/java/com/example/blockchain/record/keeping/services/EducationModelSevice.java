package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.annotation.Auditable;
import com.example.blockchain.record.keeping.aspect.AuditingContext;
import com.example.blockchain.record.keeping.dtos.request.EducationModeRequest;
import com.example.blockchain.record.keeping.dtos.request.RatingRequest;
import com.example.blockchain.record.keeping.enums.ActionType;
import com.example.blockchain.record.keeping.enums.Entity;
import com.example.blockchain.record.keeping.enums.LogTemplate;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.ActionChange;
import com.example.blockchain.record.keeping.models.EducationMode;
import com.example.blockchain.record.keeping.models.Log;
import com.example.blockchain.record.keeping.models.Rating;
import com.example.blockchain.record.keeping.repositorys.ActionChangeRepository;
import com.example.blockchain.record.keeping.repositorys.EducationModelRepository;
import com.example.blockchain.record.keeping.repositorys.LogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EducationModelSevice implements IEducationModelSevice {
    private final EducationModelRepository educationModelRepository;
    private final AuditLogService auditLogService;
    private final HttpServletRequest httpServletRequest;
    private final LogRepository logRepository;
    private final ActionChangeRepository actionChangeRepository;

    @Override
    public EducationMode findByName(String name) {
        return educationModelRepository.findByNameAndStatus( name,Status.ACTIVE)
                .orElse(null);
    }

    @Override
    public EducationMode findById(Long id) {
        return educationModelRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy hình thức đào tạo với id= "+ id));
    }

    @Override
    public List<EducationMode> listEducationMode() {
        return educationModelRepository.findByStatus(Status.ACTIVE);
    }

    @Override
    public EducationMode update(EducationMode educationMode, String name) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        EducationMode educationModeOld =  auditLogService.cloneEducationMode(educationMode);

        educationMode.setName(name);
        educationMode.setUpdatedAt(vietnamTime.toLocalDateTime());
        educationModelRepository.save(educationMode);

        List<ActionChange> changes = auditLogService.compareObjects(null, educationModeOld, educationMode);
        String ipAdress = auditLogService.getClientIp(httpServletRequest);
        if (!changes.isEmpty()) {// nếu khác old mới lưu
            Log log = new Log();
            log.setUser(auditLogService.getCurrentUser());
            log.setActionType(ActionType.UPDATED);
            log.setEntityName(Entity.rating);
            log.setEntityId(educationMode.getId());
            log.setDescription(LogTemplate.UPDATE_EDUCATION_MODE.format(educationMode.getName()));
            log.setIpAddress(ipAdress);
            log.setCreatedAt(vietnamTime.toLocalDateTime());

            log = logRepository.save(log);

            for (ActionChange change : changes) {
                change.setLog(log);
            }
            actionChangeRepository.saveAll(changes);
        }

        return educationMode;
    }

    @Override
    @Auditable(action = ActionType.DELETED, entity = Entity.educationMode )
    public EducationMode delete(EducationMode educationMode) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        educationMode.setStatus(Status.DELETED);
        educationMode.setUpdatedAt(vietnamTime.toLocalDateTime());
        educationModelRepository.save(educationMode);
        AuditingContext.setDescription("Xóa hình thức đào tạo của văn bằng có tên: " + educationMode.getName());
        return educationMode;
    }

    @Override
    public boolean findByNameAndStatus(String name, Status status) {
        return educationModelRepository.existsByNameAndStatus(name.trim(),status);
    }

    @Override
    @Auditable(action = ActionType.CREATED, entity = Entity.educationMode)
    public EducationMode add(EducationModeRequest request) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        EducationMode educationMode = new EducationMode();
        educationMode.setName(request.getName());
        educationMode.setStatus(Status.ACTIVE);
        educationMode.setCreatedAt(vietnamTime.toLocalDateTime());
        educationMode.setUpdatedAt(vietnamTime.toLocalDateTime());
        educationModelRepository.save(educationMode);
        AuditingContext.setDescription("Tạo hình thức đào tạo của văn bằng có tên: " + educationMode.getName());
        return educationMode;
    }

    @Override
    public boolean existsByNameAndStatusAndIdNot(String name, Status status, Long id) {
        return educationModelRepository.existsByNameAndStatusAndIdNot(name, status, id);
    }
}
