package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.dtos.request.DegreeTitleRequest;
import com.example.blockchain.record.keeping.dtos.request.EducationModeRequest;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.DegreeTitle;
import com.example.blockchain.record.keeping.models.EducationMode;
import com.example.blockchain.record.keeping.response.*;
import com.example.blockchain.record.keeping.services.DegreeTitleSevice;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DegreeTitleController {
    private final DegreeTitleSevice degreeTitleSevice;

    @PreAuthorize("(hasAnyRole('PDT', 'KHOA')) and hasAuthority('READ')")
    @GetMapping("/degree-title")
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

    //thêm
    @PreAuthorize("hasAuthority('WRITE')")
    @PostMapping("/pdt/degree-title")
    public ResponseEntity<?> createCertificateType(@RequestBody DegreeTitleRequest request) {
        try {
            if(request.getName() == null || !StringUtils.hasText(request.getName())){
                return ApiResponseBuilder.badRequest("Vui lòng nhập đầy đủ thông tin!");
            }
            if(degreeTitleSevice.findByNameAndStatus(request.name.trim(), Status.ACTIVE)){
                return ApiResponseBuilder.badRequest("Tên danh hiệu này đã tồn tại");
            }
            degreeTitleSevice.add(request);
            return ApiResponseBuilder.success("Thêm thành công", null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Có lỗi xảy ra: " + e.getMessage());
        }
    }

    //sửa
    @PostMapping("/pdt/degree-title/{id}")
    public ResponseEntity<?> update(
            @PathVariable("id")  Long id,
            @RequestBody EducationModeRequest request
    ){
        try{
            if(request.getName() == null || !StringUtils.hasText(request.getName())){
                return ApiResponseBuilder.badRequest("Vui lòng nhập đầy đủ thông tin!");
            }
            DegreeTitle degreeTitle = degreeTitleSevice.findById(id);

            if(request.getName().trim().equals(degreeTitle.getName().trim())){
                return ApiResponseBuilder.badRequest("Dữ liệu nhập vào chưa được thay đổi!");
            }
            if(degreeTitleSevice.findByNameAndStatus(request.name.trim(), Status.ACTIVE)){
                return ApiResponseBuilder.badRequest("Tên danh hiệu này đã tồn tại");
            }

            degreeTitleSevice.update(degreeTitle, request.getName());
            return ApiResponseBuilder.success("Cập nhật thành công", null);

        } catch (Exception e) {
            return ApiResponseBuilder.badRequest("Cập nhật thất bại!");
        }
    }

    //xóa
    @PreAuthorize("hasAuthority('READ')")
    @DeleteMapping("/pdt/degree-title/{id}")
    public ResponseEntity<?> delete(
            @PathVariable("id")  Long id)
    {
        try {
            DegreeTitle degreeTitle = degreeTitleSevice.findById(id);
            if(degreeTitle.getStatus().equals(Status.DELETED)){
                return ApiResponseBuilder.badRequest("Danh hiệu này đã bị xóa rồi");
            }
            degreeTitleSevice.delete(degreeTitle);
            return ApiResponseBuilder.success("Xóa thành công", null);
        } catch (Exception e) {
            return ApiResponseBuilder.badRequest(e.getMessage());
        }
    }
}
