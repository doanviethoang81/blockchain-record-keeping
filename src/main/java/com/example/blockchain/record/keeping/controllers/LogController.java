package com.example.blockchain.record.keeping.controllers;

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

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class LogController {

    private final UserService userService;
    private final LogService logService;
    private final ActionChangeService actionChangeService;

    // all log cua 1 tr
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/log")
    public ResponseEntity<?> getLogOfUniversity(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);

            long totalItems = logService.countLogOfUser(user.getId());

            if (totalItems == 0) {
                PaginationMeta meta = new PaginationMeta(0, 0, size, page, 0);
                PaginatedData<CertificateResponse> data = new PaginatedData<>(Collections.emptyList(), meta);
                return ApiResponseBuilder.success("Không có lịch sử nào!", data);
            }

            int offset = (page - 1) * size;

            if (offset >= totalItems && totalItems > 0) {
                page = 1;
                offset = 0;
            }

            List<LogResponse> logResponseList = logService.listLogOfUser(user.getId(),size,offset);

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
            @RequestParam(required = false) Long departmentId
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);

            long totalItems = logService.countLogDepartmentOfUniversity(user.getId(),departmentId);

            if (totalItems == 0) {
                PaginationMeta meta = new PaginationMeta(0, 0, size, page, 0);
                PaginatedData<CertificateResponse> data = new PaginatedData<>(Collections.emptyList(), meta);
                return ApiResponseBuilder.success("Không có lịch sử nào!", data);
            }

            int offset = (page - 1) * size;

            if (offset >= totalItems && totalItems > 0) {
                page = 1;
                offset = 0;
            }

            List<LogResponse> logResponseList = logService.listLogDepartmentOfUniversity(user.getId(),departmentId,size,offset);

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
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);

            long totalItems = logService.countLogOfUser(user.getId());

            if (totalItems == 0) {
                PaginationMeta meta = new PaginationMeta(0, 0, size, page, 0);
                PaginatedData<CertificateResponse> data = new PaginatedData<>(Collections.emptyList(), meta);
                return ApiResponseBuilder.success("Không có lịch sử nào!", data);
            }

            int offset = (page - 1) * size;

            if (offset >= totalItems && totalItems > 0) {
                page = 1;
                offset = 0;
            }

            List<LogResponse> logResponseList = logService.listLogOfUser(user.getId(),size,offset);

            int totalPages = (int) Math.ceil((double) totalItems / size);
            PaginationMeta meta = new PaginationMeta(totalItems, logResponseList.size(), size, page, totalPages);
            PaginatedData<LogResponse> data = new PaginatedData<>(logResponseList, meta);

            return ApiResponseBuilder.success("Danh sách lịch sử hoạt động của khoa", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    @PreAuthorize("(hasAnyRole('ADMIN', 'PDT', 'KHOA')) and hasAuthority('READ')")
    @GetMapping("/log/{id}")
    public ResponseEntity<?> getLogDetail(
            @PathVariable Long id
    ) {
        try {
            List<ActionChangeResponse> list = actionChangeService.getActionChange(id);
            return ApiResponseBuilder.success("Chi tiết lịch sử", list);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }
}
