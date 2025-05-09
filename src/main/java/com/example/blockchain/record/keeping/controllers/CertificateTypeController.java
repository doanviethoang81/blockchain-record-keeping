package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.models.CertificateType;
import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.models.UniversityCertificateType;
import com.example.blockchain.record.keeping.repositorys.UniversityCertificateTypeRepository;
import com.example.blockchain.record.keeping.repositorys.UniversityRepository;
import com.example.blockchain.record.keeping.services.CertificateTypeService;
import com.example.blockchain.record.keeping.services.UniversityCertificateTypeService;
import com.example.blockchain.record.keeping.services.UniversityService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;


@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
public class CertificateTypeController {

    private final CertificateTypeService certificateTypeService;
    private final UniversityService universityService;
    private final UniversityCertificateTypeService universityCertificateTypeService;

    @PreAuthorize("hasAuthority('WRITE')")
    @GetMapping("/check-role")
    public ResponseEntity<?> checkRole(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return ResponseEntity.ok(authorities);  // Kết quả sẽ là: ROLE_PDT, WRITE, READ
    }

    //---------------------------- ADMIN -------------------------------------------------------
    @GetMapping("/pdt/certificate_type")
    public ResponseEntity<?> getCertificateType(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<CertificateType> certificateTypes = certificateTypeService.getAll(pageable);
            return ResponseEntity.ok(certificateTypes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không có giấy chứng nhận nào!");
        }
    }

    @PreAuthorize("hasAuthority('WRITE')")
    @PostMapping("/pdt/certificate_type/create")
    public ResponseEntity<?> createCertificateType(@RequestBody CertificateType certificateType){
        try {
            if (    certificateType.getName() == null ||
                    certificateType.getName().isEmpty()) {
                return ResponseEntity.badRequest().body("Vui lòng nhập đầy đủ thông tin!");
            }
            certificateTypeService.createCertificateType(certificateType);

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            University university = universityService.getUniversityByEmail(username);
            UniversityCertificateType universityCertificateType = new UniversityCertificateType();
            universityCertificateType.setCertificateType(certificateType);
            universityCertificateType.setUniversity(university);
            universityCertificateTypeService.save(universityCertificateType);
            return ResponseEntity.ok("Thêm loại chứng chỉ thành công");
        } catch (Exception e) {
            e.printStackTrace(); // thêm dòng này
            return ResponseEntity.internalServerError().body("Có lỗi xảy ra: " + e.getMessage());
        }
    }
//---------------------------- PDT -------------------------------------------------------



//---------------------------- KHOA -------------------------------------------------------



}
