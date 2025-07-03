package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.models.University;
import org.springframework.format.annotation.DateTimeFormat;
import com.example.blockchain.record.keeping.models.User;
import com.example.blockchain.record.keeping.response.*;
import com.example.blockchain.record.keeping.services.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class LogController {

    private final UserService userService;
    private final LogService logService;
    private final UniversityService universityService;

    // all log cua 1 tr
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/log")
    public ResponseEntity<?> getLogOfUniversity(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String actionType,
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

            if (actionType != null && actionType.trim().isEmpty()) {
                actionType = null;
            }

            long totalItems = logService.countLogOfUser(user.getId(),actionType,startDate,endDate);

            if (totalItems == 0) {
                PaginationMeta meta = new PaginationMeta(0, 0, size, page, 0);
                PaginatedData<LogResponse> data = new PaginatedData<>(Collections.emptyList(), meta);
                return ApiResponseBuilder.success("Không có lịch sử nào!", data);
            }

            int offset = (page - 1) * size;

            if (offset >= totalItems && totalItems > 0) {
                page = 1;
                offset = 0;
            }

            List<LogResponse> logResponseList = logService.listLogOfUser(user.getId(),actionType,startDate,endDate,size,offset);

            int totalPages = (int) Math.ceil((double) totalItems / size);
            PaginationMeta meta = new PaginationMeta(totalItems, logResponseList.size(), size, page, totalPages);
            PaginatedData<LogResponse> data = new PaginatedData<>(logResponseList, meta);

            return ApiResponseBuilder.success("Danh sách lịch sử hoạt động của trường", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    // log của các department thuộc uni
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/log/department")
    public ResponseEntity<?> getLogDepartmentOfUniversity(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        try {
            if (page < 1) page = 1;
            if (size < 1) size = 10;

            LocalDateTime now = LocalDateTime.now();
            if (endDate == null) endDate = now;
            if (startDate == null) startDate = now.minusDays(365);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            University university = universityService.getUniversityByEmail(username);

            if (actionType != null && actionType.trim().isEmpty()) {
                actionType = null;
            }

            long totalItems = logService.countLogDepartmentOfUniversity(university.getId(),departmentId, actionType, startDate,endDate);

            if (totalItems == 0) {
                PaginationMeta meta = new PaginationMeta(0, 0, size, page, 0);
                PaginatedData<LogResponse> data = new PaginatedData<>(Collections.emptyList(), meta);
                return ApiResponseBuilder.success("Không có lịch sử nào!", data);
            }

            int offset = (page - 1) * size;

            if (offset >= totalItems && totalItems > 0) {
                page = 1;
                offset = 0;
            }

            List<LogResponse> logResponseList = logService.listLogDepartmentOfUniversity(university.getId(),departmentId,actionType,startDate,endDate, size,offset);

            int totalPages = (int) Math.ceil((double) totalItems / size);
            PaginationMeta meta = new PaginationMeta(totalItems, logResponseList.size(), size, page, totalPages);
            PaginatedData<LogResponse> data = new PaginatedData<>(logResponseList, meta);

            return ApiResponseBuilder.success("Danh sách lịch sử hoạt động của các khoa", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    // all log cua 1 khoa
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/khoa/log")
    public ResponseEntity<?> getLogOfDepartment(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        try {
            if (page < 1) page = 1;
            if (size < 1) size = 10;

            LocalDateTime now = LocalDateTime.now();
            if (endDate == null) endDate = now;
            if (startDate == null) startDate = now.minusDays(365);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);
            if (actionType != null && actionType.trim().isEmpty()) {
                actionType = null;
            }

            long totalItems = logService.countLogOfUser(user.getId(),actionType,startDate,endDate);

            if (totalItems == 0) {
                PaginationMeta meta = new PaginationMeta(0, 0, size, page, 0);
                PaginatedData<LogResponse> data = new PaginatedData<>(Collections.emptyList(), meta);
                return ApiResponseBuilder.success("Không có lịch sử nào!", data);
            }

            int offset = (page - 1) * size;

            if (offset >= totalItems && totalItems > 0) {
                page = 1;
                offset = 0;
            }

            List<LogResponse> logResponseList = logService.listLogOfUser(user.getId(),actionType,startDate,endDate, size,offset);

            int totalPages = (int) Math.ceil((double) totalItems / size);
            PaginationMeta meta = new PaginationMeta(totalItems, logResponseList.size(), size, page, totalPages);
            PaginatedData<LogResponse> data = new PaginatedData<>(logResponseList, meta);

            return ApiResponseBuilder.success("Danh sách lịch sử hoạt động của khoa", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }
}
