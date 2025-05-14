package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.dtos.CertificateTypeDTO;
import com.example.blockchain.record.keeping.dtos.request.CertificateTypeRequest;
import com.example.blockchain.record.keeping.models.CertificateType;
import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.models.UniversityCertificateType;
import com.example.blockchain.record.keeping.models.User;
import com.example.blockchain.record.keeping.response.ApiResponseBuilder;
import com.example.blockchain.record.keeping.response.CertificateTypeResponse;
import com.example.blockchain.record.keeping.response.PaginationMeta;
import com.example.blockchain.record.keeping.response.PaginatedData;
import com.example.blockchain.record.keeping.services.CertificateTypeService;
import com.example.blockchain.record.keeping.services.UniversityCertificateTypeService;
import com.example.blockchain.record.keeping.services.UniversityService;
import com.example.blockchain.record.keeping.services.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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

    @PreAuthorize("hasAuthority('WRITE')")
    @GetMapping("/check-role")
    public ResponseEntity<?> checkRole(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return ApiResponseBuilder.success("Lấy quyền thành công", authorities);
    }

    //---------------------------- PDT -------------------------------------------------------
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/certificate_type")
    public ResponseEntity<?> getCertificateTypePDT(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            University university = universityService.getUniversityByEmail(username);

            List<CertificateTypeDTO> allResult = universityCertificateTypeService
                    .listUniversityCertificateTypes(university)
                    .stream()
                    .map(u -> new CertificateTypeDTO(
                            u.getCertificateType().getId(),
                            u.getCertificateType().getName()
                    ))
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(allResult)) {
                return ApiResponseBuilder.success("Không có giấy chứng nhận nào!", null);
            }

            int start = page * size;
            int end = Math.min(start + size, allResult.size());
            if (start > end) {
                PaginatedData<CertificateTypeDTO> emptyData = new PaginatedData<>(List.of(),
                        new PaginationMeta(allResult.size(), 0, size, page + 1,
                                (int) Math.ceil((double) allResult.size() / size)));
                return ApiResponseBuilder.success("Không có dữ liệu.", emptyData);
            }

            List<CertificateTypeDTO> pagedResult = allResult.subList(start, end);
            PaginatedData<CertificateTypeDTO> data = new PaginatedData<>(pagedResult,
                    new PaginationMeta(allResult.size(), pagedResult.size(), size, page + 1,
                            (int) Math.ceil((double) allResult.size() / size)));

            return ApiResponseBuilder.success("Lấy danh sách loại chứng chỉ cho trường thành công.", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    //ct
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/certificate_type-detail/{id}")
    public ResponseEntity<?> certificateTypeDetail(@PathVariable("id")  Long id)
    {
        try {
            CertificateType certificateType = certificateTypeService.findById(id);
            return ApiResponseBuilder.success("Thông tin chi tiết chứng chỉ", certificateType);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    //sửa
//    @PreAuthorize("hasAuthority('READ')")
//    @GetMapping("/pdt/certificate_type-detail/{id}")
//    public ResponseEntity<?> updateCertificateTypeDetail(@PathVariable("id")  Long id)
//    {
//        try {
//            CertificateType certificateType = certificateTypeService.findById(id);
//            return ApiResponseBuilder.success("Thông tin chi tiết chứng chỉ", certificateType);
//        } catch (Exception e) {
//            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
//        }
//    }

    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/search-name-certificate")
    public ResponseEntity<?> searchCertificates(@RequestParam String name) {
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            University university = universityService.getUniversityByEmail(username);
            List<CertificateType> result = certificateTypeService.searchByUniversityAndName(university.getId(), name);
            List<CertificateTypeResponse> names = result.stream()
                    .map(cert -> new CertificateTypeResponse(cert.getName()))
                    .collect(Collectors.toList());

            String message = names.isEmpty() ? "Không tìm thấy chứng chỉ!" : "Tìm thành công";

            return ApiResponseBuilder.success(message,names);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //tạo
    @PreAuthorize("hasAuthority('WRITE')")
    @PostMapping("/pdt/certificate_type/create")
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

    //---------------------------- KHOA -------------------------------------------------------

    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/khoa/certificate_type")
    public ResponseEntity<?> getCertificateTypeKhoa() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userService.findByUser(username);

            List<CertificateTypeDTO> result = universityCertificateTypeService
                    .listUniversityCertificateType(user.getUniversity())
                    .stream()
                    .map(u -> new CertificateTypeDTO(
                            u.getCertificateType().getId(),
                            u.getCertificateType().getName()
                    ))
                    .collect(Collectors.toList());

            if (CollectionUtils.isEmpty(result)) {
                return ApiResponseBuilder.success("Không có giấy chứng nhận nào!", null);
            }

            return ApiResponseBuilder.success("Lấy danh sách loại chứng chỉ cho khoa thành công.", result);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }


}
