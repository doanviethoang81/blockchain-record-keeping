package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.dtos.CertificateTypeDTO;
import com.example.blockchain.record.keeping.models.CertificateType;
import com.example.blockchain.record.keeping.models.Rating;
import com.example.blockchain.record.keeping.models.User;
import com.example.blockchain.record.keeping.response.ApiResponseBuilder;
import com.example.blockchain.record.keeping.response.PaginatedData;
import com.example.blockchain.record.keeping.response.PaginationMeta;
import com.example.blockchain.record.keeping.response.RatingResponse;
import com.example.blockchain.record.keeping.services.RatingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
public class RatingController {
    private final RatingService ratingService;

    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/khoa/rating")
    public ResponseEntity<?> getRating(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        try {
            if (page < 1) page = 1;
            if (size < 1) size = 10;
            List<RatingResponse> ratingResponses = new ArrayList<>();
            List<Rating> result= ratingService.listRating();
            for(Rating rating : result){
                RatingResponse RatingResponse = new RatingResponse(
                        rating.getId(),
                        rating.getName()
                );
                ratingResponses.add(RatingResponse);
            }
            int start = (page-1) * size;
            int end = Math.min(start + size, ratingResponses.size());
            if (start >= result.size()) {
                return ApiResponseBuilder.success("Không có xếp loại nào!",result);
            }
            List<RatingResponse> pagedResult = ratingResponses.subList(start, end);
            PaginatedData<RatingResponse> data = new PaginatedData<>(pagedResult,
                    new PaginationMeta(ratingResponses.size(), pagedResult.size(), size, page ,
                            (int) Math.ceil((double) ratingResponses.size() / size)));
            return ApiResponseBuilder.success("Danh sách các loại xếp loại", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }
}
