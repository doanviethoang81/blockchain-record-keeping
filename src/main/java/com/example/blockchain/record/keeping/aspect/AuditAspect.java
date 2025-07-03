package com.example.blockchain.record.keeping.aspect;

import com.example.blockchain.record.keeping.annotation.Auditable;
import com.example.blockchain.record.keeping.enums.ActionType;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.ActionChangeRepository;
import com.example.blockchain.record.keeping.repositorys.LogRepository;
import com.example.blockchain.record.keeping.services.UserService;
import com.example.blockchain.record.keeping.utils.LogMessageTemplate;
import jakarta.persistence.Id;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Aspect
@Component
public class AuditAspect {
    @Autowired
    private LogRepository logRepository;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private UserService userService;

    @Autowired
    private ActionChangeRepository actionChangeRepository;

    @Around("@annotation(auditable)")
    public Object logAudit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        Object result = joinPoint.proceed();

        String ipAdress = getClientIp(request);
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        String description = LogMessageTemplate.getMessage(auditable.entity(),auditable.action());

        // Ghi log
        Log log = new Log();
        log.setUser(getCurrentUser());
        log.setActionType(auditable.action());
        Long entityId = extractIdFromObject(result);
        log.setEntityId(entityId);
        log.setEntityName(auditable.entity());
        log.setDescription(description);
        log.setIpAddress(ipAdress);
        log.setCreatedAt(vietnamTime.toLocalDateTime());

        logRepository.save(log);
        return result;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userService.findByUser(username);
        return user;
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        return xfHeader != null ? xfHeader.split(",")[0] : request.getRemoteAddr();
    }

    public Long extractIdFromObject(Object obj) {
        if (obj == null) return null;

        // Trường hợp truyền trực tiếp ID (Long, UUID, String,...)
        if (obj instanceof Long || obj instanceof String) {
            try {
                return Long.valueOf(obj.toString());
            } catch (NumberFormatException e) {
                return null;
            }
        }

        // Trường hợp truyền object (entity), cần tìm field @Id hoặc tên "id"
        Field[] fields = obj.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);

            // Ưu tiên tìm @Id
            if (field.isAnnotationPresent(Id.class)) {
                try {
                    Object value = field.get(obj);
                    if (value != null) return Long.valueOf(value.toString());
                } catch (IllegalAccessException ignored) {}
            }

            // Nếu không có @Id, fallback theo tên "id" hoặc chứa "Id"
            if (field.getName().equalsIgnoreCase("id") || field.getName().toLowerCase().endsWith("id")) {
                try {
                    Object value = field.get(obj);
                    if (value != null) return Long.valueOf(value.toString());
                } catch (IllegalAccessException ignored) {}
            }
        }

        return null;
    }
}


