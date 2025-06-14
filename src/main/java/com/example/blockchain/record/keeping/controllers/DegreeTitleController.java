package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.models.DegreeTitle;
import com.example.blockchain.record.keeping.models.EducationMode;
import com.example.blockchain.record.keeping.response.*;
import com.example.blockchain.record.keeping.services.DegreeTitleSevice;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DegreeTitleController {
    private final DegreeTitleSevice degreeTitleSevice;

    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/khoa/degree-title")
    public ResponseEntity<?> getDegreeTitle(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        try {
            if (page < 1) page = 1;
            if (size < 1) size = 10;
            List<DegreeTitleResponse> degreeTitleResponses = new ArrayList<>();
            List<DegreeTitle> result= degreeTitleSevice.listDegree();
            for(DegreeTitle degreeTitle : result){
                DegreeTitleResponse degreeTitleResponse = new DegreeTitleResponse(
                        degreeTitle.getId(),
                        degreeTitle.getName()
                );
                degreeTitleResponses.add(degreeTitleResponse);
            }
            int start = (page-1) * size;
            int end = Math.min(start + size, degreeTitleResponses.size());
            if (start >= result.size()) {
                return ApiResponseBuilder.success("Không có danh hiệu nào!",result);
            }
            List<DegreeTitleResponse> pagedResult = degreeTitleResponses.subList(start, end);
            PaginatedData<DegreeTitleResponse> data = new PaginatedData<>(pagedResult,
                    new PaginationMeta(degreeTitleResponses.size(), pagedResult.size(), size, page ,
                            (int) Math.ceil((double) degreeTitleResponses.size() / size)));
            return ApiResponseBuilder.success("Danh sách các loại danh hiệu văn bằng", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }
}
