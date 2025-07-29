package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.configs.Constants;
import com.example.blockchain.record.keeping.enums.NotificationType;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.response.*;
import com.example.blockchain.record.keeping.services.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationReceiverService notificationReceiverService;
    private final UserService userService;
    private final CertificateService certificateService;
    private final DegreeService degreeService;
    private final NotificateService notificateService;


    @PreAuthorize("(hasAnyRole('PDT', 'KHOA')) and hasAuthority('READ')")
    @GetMapping("/notification")
    public ResponseEntity<?> getNotification(
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
                    s.getId(),
                    s.getNotification().getTitle(),
                    s.getNotification().getContent(),
                    s.getNotification().getRejectedNote(),
                    s.getNotification().getType(),
                    s.isRead(),
                    s.getNotification().getDocumentType(),
                    s.getNotification().getDocumentId(),
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

    // XEM CHI TIẾT
    @PreAuthorize("(hasAnyRole('PDT', 'KHOA')) and hasAuthority('READ')")
    @GetMapping("/notification-detail")
    public ResponseEntity<?> notificationDetail(
            @RequestParam Long notificationId,
            @RequestParam String documentType,
            @RequestParam Long documentId
    ) {
        try {
            NotificationReceivers notification = notificationReceiverService.findById(notificationId);
            notificationReceiverService.isRead(notification);
            switch (documentType.toUpperCase()) {
                case "CERTIFICATE":
                    return handleCertificateDetail(documentId);
                case "DEGREE":
                    return handleDegreeDetail(documentId);
                default:
                    return ApiResponseBuilder.badRequest("Loại giấy tờ không hợp lệ: " + documentType);
            }
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    public ResponseEntity<?> handleCertificateDetail(Long documentId){
        try {
            Certificate certificate = certificateService.findById(documentId);
            Notifications notifications =notificateService.findByTypeAndDocumentId(NotificationType.CERTIFICATE_REJECTED, documentId);
            if( notifications == null) {
                notifications = new Notifications();
                notifications.setRejectedNote(null);
            }

            String ipfsUrl = certificate.getIpfsUrl() != null ? Constants.IPFS_URL + certificate.getIpfsUrl() : null;
            CertificateDetailResponse certificateDetailResponse = new CertificateDetailResponse(
                    certificate.getId(),
                    certificate.getStudent().getId(),
                    certificate.getStudent().getName(),
                    certificate.getStudent().getStudentClass().getName(),
                    certificate.getStudent().getStudentClass().getDepartment().getName(),
                    certificate.getStudent().getStudentClass().getDepartment().getUniversity().getName(),
                    certificate.getUniversityCertificateType().getCertificateType().getId(),
                    certificate.getUniversityCertificateType().getCertificateType().getName(),
                    certificate.getIssueDate(),
                    certificate.getDiplomaNumber(),
                    certificate.getStudent().getStudentCode(),
                    certificate.getStudent().getEmail(),
                    certificate.getStudent().getBirthDate(),
                    certificate.getStudent().getCourse(),
                    certificate.getGrantor(),
                    certificate.getSigner(),
                    certificate.getStatus().getLabel(),
                    certificate.getImageUrl(),
                    ipfsUrl,
                    certificate.getQrCodeUrl(),
                    certificate.getBlockchainTxHash(),
                    certificate.getUpdatedAt(),
                    notifications.getRejectedNote()
            );
            return ApiResponseBuilder.success("Chi tiết chứng chỉ", certificateDetailResponse);
        } catch (IllegalArgumentException e) {
            return ApiResponseBuilder.badRequest(e.getMessage());
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi " + e.getMessage());
        }
    }

    public ResponseEntity<?> handleDegreeDetail(Long documentId){
        try {
            Degree degree = degreeService.findById(documentId);
            if (degree == null) {
                return ApiResponseBuilder.badRequest("Không tìm thấy văn bằng có id =" + documentId);
            }


            DegreeDetailResponse degreeDetailResponse = new DegreeDetailResponse();
            Notifications notifications= notificateService.findByTypeAndDocumentId(NotificationType.DEGREE_REJECTED, documentId);
            if(notifications != null) {
                degreeDetailResponse.setRejectedNote(notifications.getRejectedNote());
            } else {
                degreeDetailResponse.setRejectedNote(null);
            }

            String ipfsUrl = degree.getIpfsUrl() != null ? Constants.IPFS_URL + degree.getIpfsUrl() : null;
            degreeDetailResponse.setId(degree.getId());
            degreeDetailResponse.setStudentId(degree.getStudent().getId());
            degreeDetailResponse.setNameStudent(degree.getStudent().getName());
            degreeDetailResponse.setClassName(degree.getStudent().getStudentClass().getName());
            degreeDetailResponse.setDepartmentName(degree.getStudent().getStudentClass().getDepartment().getName());
            degreeDetailResponse.setUniversity(degree.getStudent().getStudentClass().getDepartment().getUniversity().getName());
            degreeDetailResponse.setStudentCode(degree.getStudent().getStudentCode());
            degreeDetailResponse.setIssueDate(degree.getIssueDate());
            degreeDetailResponse.setGraduationYear(degree.getGraduationYear());
            degreeDetailResponse.setEmail(degree.getStudent().getEmail());
            degreeDetailResponse.setBirthDate(degree.getStudent().getBirthDate());
            degreeDetailResponse.setRatingId(degree.getRating().getId());
            degreeDetailResponse.setRatingName(degree.getRating().getName());
            degreeDetailResponse.setDegreeTitleId(degree.getDegreeTitle().getId());
            degreeDetailResponse.setDegreeTitleName(degree.getDegreeTitle().getName());
            degreeDetailResponse.setEducationModeId(degree.getEducationMode().getId());
            degreeDetailResponse.setEducationModeName(degree.getEducationMode().getName());
            degreeDetailResponse.setCourse(degree.getStudent().getCourse());
            degreeDetailResponse.setSigner(degree.getSigner());
            degreeDetailResponse.setStatus(degree.getStatus());
            degreeDetailResponse.setImageUrl(degree.getImageUrl());
            degreeDetailResponse.setIpfsUrl(ipfsUrl);
            degreeDetailResponse.setQrCodeUrl(degree.getQrCode());
            degreeDetailResponse.setTransactionHash(degree.getBlockchainTxHash());
            degreeDetailResponse.setDiplomaNumber(degree.getDiplomaNumber());
            degreeDetailResponse.setLotteryNumber(degree.getLotteryNumber());
            degreeDetailResponse.setCreatedAt(degree.getUpdatedAt());
            return ApiResponseBuilder.success("Chi tiết văn bằng", degreeDetailResponse);
        } catch (IllegalArgumentException e) {
            return ApiResponseBuilder.badRequest(e.getMessage());
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi " + e.getMessage());
        }
    }

    @PreAuthorize("(hasAnyRole('PDT', 'KHOA')) and hasAuthority('READ')")
    @PostMapping("/notification/read-all")
    public ResponseEntity<?> markAllNotificationsAsRead() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);

            notificationReceiverService.markAllAsRead(user.getId());
            return ApiResponseBuilder.success("Đã đánh dấu đã đọc thông báo", null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }
}
