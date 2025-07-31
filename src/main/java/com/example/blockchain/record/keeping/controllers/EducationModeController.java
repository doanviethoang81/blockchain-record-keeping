package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.dtos.request.EducationModeRequest;
import com.example.blockchain.record.keeping.dtos.request.RatingRequest;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.EducationMode;
import com.example.blockchain.record.keeping.models.Rating;
import com.example.blockchain.record.keeping.response.*;
import com.example.blockchain.record.keeping.services.DegreeService;
import com.example.blockchain.record.keeping.services.EducationModelSevice;
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
public class EducationModeController {

    private final EducationModelSevice educationModelSevice;
    private final DegreeService degreeService;

    @PreAuthorize("(hasAnyRole('PDT', 'KHOA')) and hasAuthority('READ')")
    @GetMapping("/education-mode")
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

    //thêm
    @PreAuthorize("hasAuthority('CREATE')")
    @PostMapping("/pdt/education-mode")
    public ResponseEntity<?> createCertificateType(@RequestBody EducationModeRequest request) {
        try {
            if(request.getName() == null || !StringUtils.hasText(request.getName())){
                return ApiResponseBuilder.badRequest("Vui lòng nhập đầy đủ thông tin!");
            }
            if(educationModelSevice.findByNameAndStatus(request.name.trim(), Status.ACTIVE)){
                return ApiResponseBuilder.badRequest("Tên hình thức đào tạo này đã tồn tại");
            }
            educationModelSevice.add(request);
            return ApiResponseBuilder.success("Thêm thành công", null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Có lỗi xảy ra: " + e.getMessage());
        }
    }

    //sửa
    @PreAuthorize("hasAuthority('UPDATE')")
    @PostMapping("/pdt/education-mode/{id}")
    public ResponseEntity<?> update(
            @PathVariable("id")  Long id,
            @RequestBody EducationModeRequest request
    ){
        try{
            if(request.getName() == null || !StringUtils.hasText(request.getName())){
                return ApiResponseBuilder.badRequest("Vui lòng nhập đầy đủ thông tin!");
            }
            EducationMode educationMode = educationModelSevice.findById(id);
            if(request.getName().trim().equals(educationMode.getName().trim())){
                return ApiResponseBuilder.badRequest("Dữ liệu nhập vào chưa được thay đổi!");
            }
            if(educationModelSevice.findByNameAndStatus(request.name.trim(), Status.ACTIVE)){
                return ApiResponseBuilder.badRequest("Tên hình thức đào tạo này đã tồn tại");
            }

            educationModelSevice.update(educationMode, request.getName());
            return ApiResponseBuilder.success("Cập nhật thành công", null);

        } catch (Exception e) {
            return ApiResponseBuilder.badRequest("Cập nhật thất bại!");
        }
    }

    //xóa
    @PreAuthorize("hasAuthority('DELETE')")
    @DeleteMapping("/pdt/education-mode/{id}")
    public ResponseEntity<?> delete(
            @PathVariable("id")  Long id)
    {
        try {
            EducationMode educationMode = educationModelSevice.findById(id);
            if(degreeService.existsByEducationModeIdAndStatusIn(id, List.of(Status.APPROVED, Status.PENDING,Status.REJECTED))){
                return ApiResponseBuilder.badRequest("Không thể xóa hình thức đào tạo này vì đã có văn bằng sử dụng");
            }
            if(educationMode.getStatus().equals(Status.DELETED)){
                return ApiResponseBuilder.badRequest("Hình thức đào tạo này đã bị xóa rồi");
            }
            educationModelSevice.delete(educationMode);
            return ApiResponseBuilder.success("Xóa thành công", null);
        } catch (Exception e) {
            return ApiResponseBuilder.badRequest(e.getMessage());
        }
    }

}
