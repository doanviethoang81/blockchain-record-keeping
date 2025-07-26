package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.annotation.Auditable;
import com.example.blockchain.record.keeping.aspect.AuditingContext;
import com.example.blockchain.record.keeping.dtos.request.RatingRequest;
import com.example.blockchain.record.keeping.enums.ActionType;
import com.example.blockchain.record.keeping.enums.Entity;
import com.example.blockchain.record.keeping.enums.LogTemplate;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.ActionChange;
import com.example.blockchain.record.keeping.models.Log;
import com.example.blockchain.record.keeping.models.Rating;
import com.example.blockchain.record.keeping.repositorys.ActionChangeRepository;
import com.example.blockchain.record.keeping.repositorys.LogRepository;
import com.example.blockchain.record.keeping.repositorys.RatingRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RatingService implements IRatingService {

    private final RatingRepository ratingRepository;
    private final AuditLogService auditLogService;
    private final HttpServletRequest httpServletRequest;
    private final LogRepository logRepository;
    private final ActionChangeRepository actionChangeRepository;

    @Override
    public Rating findByName(String name) {
        return ratingRepository.findByName(name)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy loại xếp hạng này!"));
    }

    @Override
    public Rating findById(Long id) {
        return ratingRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy loại xếp hạng với id="+ id));
    }

    @Override
    public List<Rating> listRating() {
        return ratingRepository.findByStatus(Status.ACTIVE);
    }

    @Override
    public Rating update(Rating rating, String name) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        Rating ratingOld =  auditLogService.cloneRating(rating);
        rating.setName(name);
        rating.setUpdatedAt(vietnamTime.toLocalDateTime());
        ratingRepository.save(rating);

        List<ActionChange> changes = auditLogService.compareObjects(null, ratingOld, rating);
        String ipAdress = auditLogService.getClientIp(httpServletRequest);
        if (!changes.isEmpty()) {// nếu khác old mới lưu
            Log log = new Log();
            log.setUser(auditLogService.getCurrentUser());
            log.setActionType(ActionType.UPDATED);
            log.setEntityName(Entity.rating);
            log.setEntityId(rating.getId());
            log.setDescription(LogTemplate.UPDATE_RATING.format(rating.getName()));
            log.setIpAddress(ipAdress);
            log.setCreatedAt(vietnamTime.toLocalDateTime());

            log = logRepository.save(log);

            for (ActionChange change : changes) {
                change.setLog(log);
            }
            actionChangeRepository.saveAll(changes);
        }
        return rating;
    }

    @Override
    @Auditable(action = ActionType.DELETED, entity = Entity.rating )
    public Rating delete(Rating rating) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        rating.setStatus(Status.DELETED);
        rating.setUpdatedAt(vietnamTime.toLocalDateTime());
        ratingRepository.save(rating);
        AuditingContext.setDescription("Xóa loại xếp loại của văn bằng có tên: " + rating.getName());
        return rating;
    }

    @Override
    public boolean findByNameAndStatus(String name, Status status) {
        return ratingRepository.existsByNameAndStatus(name.trim(),status);
    }

    @Override
    @Auditable(action = ActionType.CREATED, entity = Entity.rating)
    public Rating add(RatingRequest request) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        Rating rating = new Rating();
        rating.setName(request.getName());
        rating.setStatus(Status.ACTIVE);
        rating.setCreatedAt(vietnamTime.toLocalDateTime());
        rating.setUpdatedAt(vietnamTime.toLocalDateTime());
        ratingRepository.save(rating);
        AuditingContext.setDescription("Tạo loại xếp loại của văn bằng có tên: " + rating.getName());
        return rating;
    }
}
