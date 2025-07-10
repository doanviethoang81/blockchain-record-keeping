package com.example.blockchain.record.keeping.services;


import com.example.blockchain.record.keeping.dtos.request.UniversityRequest;
import com.example.blockchain.record.keeping.enums.ActionType;
import com.example.blockchain.record.keeping.enums.Entity;
import com.example.blockchain.record.keeping.enums.LogTemplate;
import com.example.blockchain.record.keeping.models.ActionChange;
import com.example.blockchain.record.keeping.models.Log;
import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.models.User;
import com.example.blockchain.record.keeping.repositorys.ActionChangeRepository;
import com.example.blockchain.record.keeping.repositorys.LogRepository;
import com.example.blockchain.record.keeping.repositorys.UniversityRepository;
import com.example.blockchain.record.keeping.repositorys.UserRepository;
import com.example.blockchain.record.keeping.response.ApiResponse;
import com.example.blockchain.record.keeping.response.ApiResponseBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UniversityService implements IUniversityService{

    private final UniversityRepository universityRepository;
    private final ActionChangeRepository actionChangeRepository;
    private final LogRepository logRepository;
    private final AuditLogService auditLogService;
    private final HttpServletRequest httpServletRequest;
    private final UserRepository userRepository;

    @Override
    public University getUniversityByEmail(String email) {
        return universityRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User không tồn tại"));
    }

    @Override
    public List<User> findAllUserUniversity(String nameUniversity) {
        return universityRepository.findAllUserUniversity(nameUniversity);
    }

    @Override
    public University findById(Long id) {
        return universityRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy trường có id "+ id));
    }

    @Override
    @Transactional
    public void update(University university , UniversityRequest universityRequest) throws IOException {
        University universityOld = auditLogService.cloneUniversity(university);

        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        User user = userRepository.findByEmail(university.getEmail()).orElse(null);

        user.setEmail(universityRequest.getEmail());
        user.setCreatedAt(vietnamTime.toLocalDateTime());
        userRepository.save(user);

        university.setName(universityRequest.getName());
        university.setEmail(universityRequest.getEmail());
        university.setAddress(universityRequest.getAddress());
        university.setTaxCode(universityRequest.getTaxCode());
        university.setWebsite(universityRequest.getWebsite());

        universityRepository.save(university);

        String ipAdress = auditLogService.getClientIp(httpServletRequest);
        List<ActionChange> changes = auditLogService.compareObjects(null, universityOld, university);
        if (!changes.isEmpty()) {// nếu khác old mới lưu
            Log log = new Log();
            log.setUser(user);
            log.setActionType(ActionType.UPDATED);
            log.setEntityName(Entity.universitys);
            log.setEntityId(null);
            log.setDescription(LogTemplate.UPDATE_UNIVERCITY.getName());
            log.setIpAddress(ipAdress);
            log.setCreatedAt(vietnamTime.toLocalDateTime());

            log = logRepository.save(log);

            for (ActionChange change : changes) {
                change.setLog(log);
            }
            actionChangeRepository.saveAll(changes);
        }
    }



    @Override
    public boolean existsByNameIgnoreCaseAndIdNot(String name, Long id) {
        return universityRepository.existsByNameIgnoreCaseAndIdNot(name,id);
    }

    @Override
    public University save(University university) {
        return universityRepository.save(university);
    }
}
