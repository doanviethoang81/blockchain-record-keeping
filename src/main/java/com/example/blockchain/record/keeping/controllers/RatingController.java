package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.dtos.CertificateTypeDTO;
import com.example.blockchain.record.keeping.dtos.request.CertificateTypeRequest;
import com.example.blockchain.record.keeping.dtos.request.RatingRequest;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.response.ApiResponseBuilder;
import com.example.blockchain.record.keeping.response.PaginatedData;
import com.example.blockchain.record.keeping.response.PaginationMeta;
import com.example.blockchain.record.keeping.response.RatingResponse;
import com.example.blockchain.record.keeping.services.CertificateService;
import com.example.blockchain.record.keeping.services.DegreeService;
import com.example.blockchain.record.keeping.services.RatingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class RatingController {
    private final RatingService ratingService;
    private final DegreeService degreeService;

    @PreAuthorize("(hasAnyRole('PDT', 'KHOA')) and hasAuthority('READ')")
    @GetMapping("/rating")
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

    //thêm
    @PreAuthorize("hasAuthority('CREATE')")
    @PostMapping("/pdt/rating")
    public ResponseEntity<?> createCertificateType(@RequestBody RatingRequest request) {
        try {
            if(request.getName() == null || !StringUtils.hasText(request.getName())){
                return ApiResponseBuilder.badRequest("Vui lòng nhập đầy đủ thông tin!");
            }
            if(ratingService.findByNameAndStatus(request.name.trim(), Status.ACTIVE)){
                return ApiResponseBuilder.badRequest("Tên xếp loại này đã tồn tại");
            }
            ratingService.add(request);
            return ApiResponseBuilder.success("Thêm thành công", null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Có lỗi xảy ra: " + e.getMessage());
        }
    }

    //sửa
    @PreAuthorize("hasAuthority('UPDATE')")
    @PostMapping("/pdt/rating/{id}")
    public ResponseEntity<?> update(
            @PathVariable("id")  Long id,
            @RequestBody RatingRequest request
    ){
        try{
            if(request.getName() == null || !StringUtils.hasText(request.getName())){
                return ApiResponseBuilder.badRequest("Vui lòng nhập đầy đủ thông tin!");
            }
            Rating rating = ratingService.findById(id);
            if(request.getName().trim().equals(rating.getName().trim())){
                return ApiResponseBuilder.badRequest("Dữ liệu nhập vào chưa được thay đổi!");
            }
            if(ratingService.findByNameAndStatus(request.name.trim(), Status.ACTIVE)){
                return ApiResponseBuilder.badRequest("Tên xếp loại này đã tồn tại");
            }
            ratingService.update(rating, request.getName());
            return ApiResponseBuilder.success("Cập nhật thành công", null);

        } catch (Exception e) {
            return ApiResponseBuilder.badRequest("Cập nhật thất bại!");
        }
    }

    //xóa
    @PreAuthorize("hasAuthority('DELETE')")
    @DeleteMapping("/pdt/rating/{id}")
    public ResponseEntity<?> delete(
            @PathVariable("id")  Long id)
    {
        try {
            Rating rating = ratingService.findById(id);
            if(degreeService.existsByRatingIdAndStatusIn(id, List.of(Status.APPROVED, Status.PENDING,Status.REJECTED))){
                return ApiResponseBuilder.badRequest("Không thể xóa xếp loại này vì đã có văn bằng sử dụng");
            }
            if(rating.getStatus().equals(Status.DELETED)){
                return ApiResponseBuilder.badRequest("Xếp loại này đã bị xóa rồi");
            }
            ratingService.delete(rating);
            return ApiResponseBuilder.success("Xóa thành công", null);
        } catch (Exception e) {
            return ApiResponseBuilder.badRequest(e.getMessage());
        }
    }
}
