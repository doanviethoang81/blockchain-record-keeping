package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.models.Certificate;
import com.example.blockchain.record.keeping.models.NotificationReceivers;
import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.models.User;
import com.example.blockchain.record.keeping.response.*;
import com.example.blockchain.record.keeping.services.NotificateService;
import com.example.blockchain.record.keeping.services.NotificationReceiverService;
import com.example.blockchain.record.keeping.services.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.sql.internal.ParameterRecognizerImpl;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificateService notificateService;
    private final NotificationReceiverService notificationReceiverService;
    private final UserService userService;


    @PreAuthorize("(hasAnyRole('PDT', 'KHOA')) and hasAuthority('READ')")
    @GetMapping("/notification")
    public ResponseEntity<?> getCertificateOfUniversity(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name = "status", defaultValue = "all") String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        if (page < 1) page = 1;
        if (size < 1) size = 10;

        LocalDateTime now = LocalDateTime.now();
        if (endDate == null) endDate = now;
        if (startDate == null) startDate = now.minusDays(365);
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);

            Boolean isRead = null;
            if ("read".equalsIgnoreCase(status)) {
                isRead = true;
            } else if ("unread".equalsIgnoreCase(status)) {
                isRead = false;
            }

            long totalItems = notificationReceiverService.countNotification(user.getId(),isRead, startDate, endDate);

            if (totalItems == 0) {
                PaginationMeta meta = new PaginationMeta(0, 0, size, page, 0);
                PaginatedData<CertificateResponse> data = new PaginatedData<>(Collections.emptyList(), meta);
                return ApiResponseBuilder.success("Không có thông báo nào!", data);
            }

            int offset = (page - 1) * size;

            if (offset >= totalItems && totalItems > 0) {
                page = 1;
                offset = 0;
            }

            List<NotificationReceivers> notifications = notificationReceiverService.notificationList(
                    user.getId(),
                    isRead,
                    startDate,
                    endDate,
                    size,
                    offset
            );

            List<NotificationResponse> notificationResponses = notifications.stream().map(s -> new NotificationResponse(
                    s.getNotification().getTitle(),
                    s.getNotification().getContent(),
                    s.getNotification().getType(),
                    s.isRead(),
                    s.getCreatedAt()
            )).collect(Collectors.toList());

            int totalPages = (int) Math.ceil((double) totalItems / size);
            PaginationMeta meta = new PaginationMeta(totalItems, notificationResponses.size(), size, page, totalPages);
            PaginatedData<NotificationResponse> data = new PaginatedData<>(notificationResponses, meta);

            return ApiResponseBuilder.success("Danh sách thông báo", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }
}
