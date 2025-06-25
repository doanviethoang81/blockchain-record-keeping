package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.dtos.CertificateTypeDTO;
import com.example.blockchain.record.keeping.dtos.request.CertificateTypeRequest;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.response.ApiResponseBuilder;
import com.example.blockchain.record.keeping.response.PaginationMeta;
import com.example.blockchain.record.keeping.response.PaginatedData;
import com.example.blockchain.record.keeping.services.CertificateTypeService;
import com.example.blockchain.record.keeping.services.UniversityCertificateTypeService;
import com.example.blockchain.record.keeping.services.UniversityService;
import com.example.blockchain.record.keeping.services.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CertificateTypeController {

    private final CertificateTypeService certificateTypeService;
    private final UniversityService universityService;
    private final UserService userService;
    private final UniversityCertificateTypeService universityCertificateTypeService;

    //---------------------------- PDT -------------------------------------------------------
    //ds loại chứng chỉ cho tr vs tìm theo ten
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/certificate-type")
    public ResponseEntity<?> getCertificateTypePDT(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name
    ) {
        try {
            if (page < 1) page = 1;
            if (size < 1) size = 10;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);
            List<CertificateTypeDTO> certificateTypeDTOS = new ArrayList<>();

            List<CertificateType> result = certificateTypeService.searchByUniversityAndName(user.getUniversity().getId(), name);

            for(CertificateType certificateType : result){
                CertificateTypeDTO certificateTypeDTO = new CertificateTypeDTO(
                        certificateType.getId(),
                        certificateType.getName()
                );
                certificateTypeDTOS.add(certificateTypeDTO);
            }
            if (name != null && !name.isEmpty()) {// tìm theo tên
                if (result.isEmpty()) {
                    return ApiResponseBuilder.success("Không tìm thấy!",result);
                }
            }

            int start = (page-1) * size;
            int end = Math.min(start + size, certificateTypeDTOS.size());
            if (start >= result.size()) {
                return ApiResponseBuilder.success("Không có loại chứng chỉ nào!",result);
            }
            List<CertificateTypeDTO> pagedResult = certificateTypeDTOS.subList(start, end);
            PaginatedData<CertificateTypeDTO> data = new PaginatedData<>(pagedResult,
                    new PaginationMeta(certificateTypeDTOS.size(), pagedResult.size(), size, page ,
                            (int) Math.ceil((double) certificateTypeDTOS.size() / size)));
            return ApiResponseBuilder.success("Danh sách loại chứng chỉ", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    //chi tiết
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/certificate-type-detail/{id}")
    public ResponseEntity<?> certificateTypeDetail(@PathVariable("id")  Long id)
    {
        try {
            CertificateType certificateType = certificateTypeService.findById(id);
            return ApiResponseBuilder.success("Thông tin chi tiết loại chứng chỉ", certificateType);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    //tạo
    @PreAuthorize("hasAuthority('WRITE')")
    @PostMapping("/pdt/certificate-type/create")
    public ResponseEntity<?> createCertificateType(@RequestBody CertificateTypeRequest certificateType) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            University university = universityService.getUniversityByEmail(username);

            if (certificateType == null || !StringUtils.hasText(certificateType.getName())) {
                return ApiResponseBuilder.badRequest("Vui lòng nhập đầy đủ thông tin!");
            }

            boolean exists = universityCertificateTypeService.existsByUniversityAndCertificateName(university, certificateType.getName());
            if (exists) {
                return ApiResponseBuilder.badRequest("Tên chứng chỉ đã tồn tại trong trường này!");
            }

            ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

            CertificateType newCertificateType = new CertificateType();
            newCertificateType.setName(certificateType.getName());
            newCertificateType.setStatus(Status.ACTIVE);
            newCertificateType.setCreatedAt(vietnamTime.toLocalDateTime());
            newCertificateType.setUpdatedAt(vietnamTime.toLocalDateTime());
            certificateTypeService.createCertificateType(newCertificateType);

            UniversityCertificateType universityCertificateType = new UniversityCertificateType();
            universityCertificateType.setCertificateType(newCertificateType);
            universityCertificateType.setUniversity(university);
            universityCertificateTypeService.save(universityCertificateType);

            return ApiResponseBuilder.success("Thêm loại chứng chỉ thành công", null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Có lỗi xảy ra: " + e.getMessage());
        }
    }

    //sửa
    @PreAuthorize("hasAuthority('READ')")
    @PutMapping("/pdt/update-certificate_type/{id}")
    public ResponseEntity<?> updateCertificateType(
            @PathVariable("id")  Long id,
            HttpServletRequest request)
    {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            University university = universityService.getUniversityByEmail(username);

            String name = request.getParameter("name");
            if(!StringUtils.hasText(name)){
                return ApiResponseBuilder.badRequest("Vui lòng nhập đầy đủ thông tin!");
            }
            boolean exists = universityCertificateTypeService.existsByUniversityAndCertificateName(university, name);
            if (exists) {
                return ApiResponseBuilder.badRequest("Tên chứng chỉ đã tồn tại trong trường này!");
            }
            certificateTypeService.update(id, name);
            return ApiResponseBuilder.success("Sửa thông tin loại chứng chỉ thành công", null);
        } catch (Exception e) {
            return ApiResponseBuilder.badRequest(e.getMessage());
        }
    }

    //xóa
    @PreAuthorize("hasAuthority('READ')")
    @DeleteMapping("/pdt/delete-certificate_type/{id}")
    public ResponseEntity<?> deleteCertificateType(
            @PathVariable("id")  Long id)
    {
        try {
            CertificateType certificateType = certificateTypeService.findById(id);
            certificateTypeService.delete(certificateType);
            return ApiResponseBuilder.success("Xóa loại chứng chỉ thành công", null);
        } catch (Exception e) {
            return ApiResponseBuilder.badRequest(e.getMessage());
        }
    }

    //---------------------------- KHOA -------------------------------------------------------
//    ds ch ch
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/khoa/certificate-type")
    public ResponseEntity<?> getCertificateTypeDepartment(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name
    ) {
        try {
            if (page < 1) page = 1;
            if (size < 1) size = 1000;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);
            List<CertificateTypeDTO> certificateTypeDTOS = new ArrayList<>();

            List<CertificateType> result = certificateTypeService.searchByUniversityAndName(user.getUniversity().getId(), name);

            for(CertificateType certificateType : result){
                CertificateTypeDTO certificateTypeDTO = new CertificateTypeDTO(
                        certificateType.getId(),
                        certificateType.getName()
                );
                certificateTypeDTOS.add(certificateTypeDTO);
            }
            if (name != null && !name.isEmpty()) {// tìm theo tên
                if (result.isEmpty()) {
                    return ApiResponseBuilder.success("Không tìm thấy!",result);
                }
            }

            int start = (page - 1) * size;
            int end = Math.min(start + size, certificateTypeDTOS.size());
            if (start >= result.size()) {
                return ApiResponseBuilder.success("Không có loại chứng chỉ nào!",result);
            }
            List<CertificateTypeDTO> pagedResult = certificateTypeDTOS.subList(start, end);
            PaginatedData<CertificateTypeDTO> data = new PaginatedData<>(pagedResult,
                    new PaginationMeta(certificateTypeDTOS.size(), pagedResult.size(), size, page ,
                            (int) Math.ceil((double) certificateTypeDTOS.size() / size)));
            return ApiResponseBuilder.success("Danh sách loại chứng chỉ", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }
}
