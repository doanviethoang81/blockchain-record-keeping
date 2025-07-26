package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.annotation.Auditable;
import com.example.blockchain.record.keeping.aspect.AuditingContext;
import com.example.blockchain.record.keeping.dtos.request.DegreeTitleRequest;
import com.example.blockchain.record.keeping.enums.ActionType;
import com.example.blockchain.record.keeping.enums.Entity;
import com.example.blockchain.record.keeping.enums.LogTemplate;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.ActionChangeRepository;
import com.example.blockchain.record.keeping.repositorys.DegreeTitleRepository;
import com.example.blockchain.record.keeping.repositorys.LogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DegreeTitleSevice implements IDegreeTitleSevice {

    private final DegreeTitleRepository degreeTitleRepository;
    private final AuditLogService auditLogService;
    private final HttpServletRequest httpServletRequest;
    private final LogRepository logRepository;
    private final ActionChangeRepository actionChangeRepository;

    @Override
    public DegreeTitle findByName(String name) {
        return degreeTitleRepository.findByName(name)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy loại văn bằng!"));
    }

    @Override
    public DegreeTitle findById(Long id) {
        return degreeTitleRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy loại văn bằng với id= "+ id));
    }

    @Override
    public List<DegreeTitle> listDegree() {
        return degreeTitleRepository.findByStatus(Status.ACTIVE);
    }

    @Override
    public DegreeTitle update(DegreeTitle degreeTitle, String name) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        DegreeTitle degreeTitleOld =  auditLogService.cloneDegreeTitle(degreeTitle);
        degreeTitle.setName(name);
        degreeTitle.setUpdatedAt(vietnamTime.toLocalDateTime());
        degreeTitleRepository.save(degreeTitle);

        List<ActionChange> changes = auditLogService.compareObjects(null, degreeTitleOld, degreeTitle);
        String ipAdress = auditLogService.getClientIp(httpServletRequest);
        if (!changes.isEmpty()) {// nếu khác old mới lưu
            Log log = new Log();
            log.setUser(auditLogService.getCurrentUser());
            log.setActionType(ActionType.UPDATED);
            log.setEntityName(Entity.degreeTitle);
            log.setEntityId(degreeTitle.getId());
            log.setDescription(LogTemplate.UPDATE_DEGREE_TITLE.format(degreeTitle.getName()));
            log.setIpAddress(ipAdress);
            log.setCreatedAt(vietnamTime.toLocalDateTime());

            log = logRepository.save(log);

            for (ActionChange change : changes) {
                change.setLog(log);
            }
            actionChangeRepository.saveAll(changes);
        }
        return degreeTitle;
    }

    @Override
    @Auditable(action = ActionType.DELETED, entity = Entity.degreeTitle )
    public DegreeTitle delete(DegreeTitle degreeTitle) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        degreeTitle.setStatus(Status.DELETED);
        degreeTitle.setUpdatedAt(vietnamTime.toLocalDateTime());
        degreeTitleRepository.save(degreeTitle);
        AuditingContext.setDescription("Xóa danh hiệu của văn bằng có tên: " + degreeTitle.getName());
        return degreeTitle;
    }

    @Override
    public boolean findByNameAndStatus(String name, Status status) {
        return degreeTitleRepository.existsByNameAndStatus(name.trim(),status);
    }

    @Override
    @Auditable(action = ActionType.CREATED, entity = Entity.degreeTitle)
    public DegreeTitle add(DegreeTitleRequest request) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        DegreeTitle degreeTitle = new DegreeTitle();
        degreeTitle.setName(request.getName());
        degreeTitle.setStatus(Status.ACTIVE);
        degreeTitle.setCreatedAt(vietnamTime.toLocalDateTime());
        degreeTitle.setUpdatedAt(vietnamTime.toLocalDateTime());
        degreeTitleRepository.save(degreeTitle);
        AuditingContext.setDescription("Tạo danh hiệu của văn bằng có tên: " + degreeTitle.getName());
        return degreeTitle;
    }
}
