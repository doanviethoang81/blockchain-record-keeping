package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.models.EducationMode;
import com.example.blockchain.record.keeping.models.Rating;
import com.example.blockchain.record.keeping.response.*;
import com.example.blockchain.record.keeping.services.EducationModelSevice;
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
public class EducationModeController {

    private final EducationModelSevice educationModelSevice;

    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/khoa/education-mode")
    public ResponseEntity<?> getEducationMode(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        try {
            if (page < 1) page = 1;
            if (size < 1) size = 10;
            List<EducationModeResponse> educationModeResponses = new ArrayList<>();
            List<EducationMode> result= educationModelSevice.listEducationMode();
            for(EducationMode educationMode : result){
                EducationModeResponse educationModeResponse = new EducationModeResponse(
                        educationMode.getId(),
                        educationMode.getName()
                );
                educationModeResponses.add(educationModeResponse);
            }
            int start = (page-1) * size;
            int end = Math.min(start + size, educationModeResponses.size());
            if (start >= result.size()) {
                return ApiResponseBuilder.success("Không có hình thức đào tạo nào!",result);
            }
            List<EducationModeResponse> pagedResult = educationModeResponses.subList(start, end);
            PaginatedData<EducationModeResponse> data = new PaginatedData<>(pagedResult,
                    new PaginationMeta(educationModeResponses.size(), pagedResult.size(), size, page ,
                            (int) Math.ceil((double) educationModeResponses.size() / size)));
            return ApiResponseBuilder.success("Danh sách các loại hình thức đào tạo", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }
}
