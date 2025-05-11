package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.dtos.CertificateTypeDTO;
import com.example.blockchain.record.keeping.dtos.request.CertificateTypeRequest;
import com.example.blockchain.record.keeping.models.CertificateType;
import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.models.UniversityCertificateType;
import com.example.blockchain.record.keeping.models.User;
import com.example.blockchain.record.keeping.repositorys.UniversityCertificateTypeRepository;
import com.example.blockchain.record.keeping.repositorys.UniversityRepository;
import com.example.blockchain.record.keeping.response.ApiResponseBuilder;
import com.example.blockchain.record.keeping.response.PaginationInfo;
import com.example.blockchain.record.keeping.services.CertificateTypeService;
import com.example.blockchain.record.keeping.services.UniversityCertificateTypeService;
import com.example.blockchain.record.keeping.services.UniversityService;
import com.example.blockchain.record.keeping.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
public class CertificateTypeController {

    private final CertificateTypeService certificateTypeService;
    private final UniversityService universityService;
    private final UserService userService;
    private final UniversityCertificateTypeService universityCertificateTypeService;

    @PreAuthorize("hasAuthority('WRITE')")
    @GetMapping("/check-role")
    public ResponseEntity<?> checkRole(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return ResponseEntity.ok(authorities);  // Kết quả sẽ là: ROLE_PDT, WRITE, READ
    }

    //---------------------------- ADMIN -------------------------------------------------------
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/certificate_type")
    public ResponseEntity<?> getCertificateTypePDT(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            University university = universityService.getUniversityByEmail(username);

            List<UniversityCertificateType> listUniversityCertificateType= universityCertificateTypeService.listUniversityCertificateType(university);

            List<CertificateTypeDTO> allResult = listUniversityCertificateType.stream()
                    .map(u -> new CertificateTypeDTO(
                            u.getCertificateType().getId(),
                            u.getCertificateType().getName()
                    ))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(allResult)) {
                return ApiResponseBuilder.badRequest("Không có giấy chứng nhận nào!");
            }

            int start = page * size;
            int end = Math.min(start + size, allResult.size());
            if (start > end) {
                return ApiResponseBuilder.success("Không có dữ liệu.", null,null);
            }

            List<CertificateTypeDTO> pagedResult = allResult.subList(start, end);
            return ApiResponseBuilder.success(
                    "Lấy danh sách loại chứng chỉ thành công.",
                    pagedResult,
                    new PaginationInfo(page, size, allResult.size(), (int) Math.ceil((double) allResult.size() / size))
            );
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Không có giấy chứng nhận nào!");
        }
    }

    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/khoa/certificate_type")
    public ResponseEntity<?> getCertificateTypeKhoa(){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);
            List<UniversityCertificateType> listUniversityCertificateType= universityCertificateTypeService.listUniversityCertificateType(user.getUniversity());

            List<CertificateTypeDTO> result = listUniversityCertificateType.stream()
                    .map(u -> new CertificateTypeDTO(
                            u.getCertificateType().getId(),
                            u.getCertificateType().getName()
                    ))
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(result)) {
                return ApiResponseBuilder.badRequest("Không có giấy chứng nhận nào!");
            }
            return ApiResponseBuilder.success("Lấy danh sách loại chứng chỉ thành công.", result,null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Không có giấy chứng nhận nào!");
        }
    }

    @PreAuthorize("hasAuthority('WRITE')")
    @PostMapping("/pdt/certificate_type/create")
    public ResponseEntity<?> createCertificateType(@RequestBody CertificateTypeRequest certificateType){
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            University university = universityService.getUniversityByEmail(username);

            if (certificateType == null ||
                    !StringUtils.hasText(certificateType.getName())){
                return ApiResponseBuilder.badRequest("Vui lòng nhập đầy đủ thông tin!");
            }
            boolean exists = universityCertificateTypeService.existsByUniversityAndCertificateName(university, certificateType.getName());
            if (exists) {
                return ApiResponseBuilder.badRequest("Tên chứng chỉ đã tồn tại trong trường này!");
            }

            CertificateType certificateTypeNew = new CertificateType();
            certificateTypeNew.setName(certificateType.getName());
            certificateTypeService.createCertificateType(certificateTypeNew);

            UniversityCertificateType universityCertificateType = new UniversityCertificateType();
            universityCertificateType.setCertificateType(certificateTypeNew);
            universityCertificateType.setUniversity(university);
            universityCertificateTypeService.save(universityCertificateType);
            return ApiResponseBuilder.success("Thêm loại chứng chỉ thành công", null,null);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponseBuilder.internalError("Có lỗi xảy ra: " + e.getMessage());
        }
    }
//---------------------------- PDT -------------------------------------------------------



//---------------------------- KHOA -------------------------------------------------------



}
