package com.example.blockchain.record.keeping.controllers.admin;

import com.example.blockchain.record.keeping.models.CertificateType;
import com.example.blockchain.record.keeping.services.CertificateTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("${api.prefix:/api/v1}/admin/certificate_type")
@RequiredArgsConstructor
public class CertificateTypeController {

    private final CertificateTypeService certificateTypeService;

    @GetMapping("")
    private ResponseEntity<?> getCertificateType(
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

    @PostMapping("/create")
    private ResponseEntity<?> createCertificateType(@RequestBody CertificateType certificateType){
        try {
            if (    certificateType.getName() == null ||
                    certificateType.getName().isEmpty()) {
                return ResponseEntity.badRequest().body("Vui lòng nhập đầy đủ thông tin!");
            }

            CertificateType certificateTypeNew = certificateTypeService.createCertificateType(certificateType);

            return ResponseEntity.ok("Thêm chứng chỉ thành công");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Có lỗi xảy ra: " + e.getMessage());
        }
    }


}
