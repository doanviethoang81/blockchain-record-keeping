package com.example.blockchain.record.keeping.controllers;

import com.alibaba.excel.EasyExcel;
import com.example.blockchain.record.keeping.configs.Constants;
import com.example.blockchain.record.keeping.dtos.CertificateExcelDTO;
import com.example.blockchain.record.keeping.dtos.CertificateExcelRowDTO;
import com.example.blockchain.record.keeping.dtos.request.CertificateRequest;
import com.example.blockchain.record.keeping.dtos.request.DecryptRequest;
import com.example.blockchain.record.keeping.dtos.request.ListValidationRequest;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.LogRepository;
import com.example.blockchain.record.keeping.response.*;
import com.example.blockchain.record.keeping.services.*;
import com.example.blockchain.record.keeping.utils.ExcelStyleUtil;
import com.example.blockchain.record.keeping.utils.RSAUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CertificateController {

    private final CertificateService certificateService;
    private final UniversityService universityService;
    private final UserService userService;
    private final StudentService studentService;
    private final CertificateTypeService certificateTypeService;
    private final UniversityCertificateTypeService universityCertificateTypeService;
    private final GraphicsTextWriter graphicsTextWriter;
    private final ObjectMapper objectMapper;
    private final RSAUtil rsaUtil;
    private final BlockChainService blockChainService;
    private final DegreeService degreeService;
    private final AuditLogService auditLogService;
    private final LogRepository logRepository;
    private final HttpServletRequest httpServletRequest;
    private final NotificateService notificateService;

    //---------------------------- ADMIN -------------------------------------------------------
    // xem all chứng chỉ
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/admin/list-certificates")
    public ResponseEntity<?> getAllCertificate(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String universityName,
            @RequestParam(required = false) String departmentName,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String studentCode,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String diplomaNumber
    ) {
        try {
            List<Certificate> certificateList = certificateService.findByAllCertificate(
                    universityName,
                    departmentName,
                    className,
                    studentCode,
                    studentName,
                    diplomaNumber
            );
            if (universityName != null && !universityName.isEmpty()
                    ||departmentName != null && !departmentName.isEmpty()
                    ||className != null && !className.isEmpty()
                    ||studentCode != null && !studentCode.isEmpty()
                    ||studentName != null && !studentName.isEmpty()
                    ||diplomaNumber != null && !diplomaNumber.isEmpty()
            ) {
                if (certificateList.isEmpty()) {
                    return ApiResponseBuilder.success("Không tìm thấy chứng chỉ ",certificateList);
                }
            }
            List<CertificateResponse> certificateResponseList = certificateList.stream()
                    .map(s -> new CertificateResponse(
                            s.getId(),
                            s.getStudent().getName(),
                            s.getStudent().getStudentClass().getName(),
                            s.getStudent().getStudentClass().getDepartment().getName(),
                            s.getIssueDate(),
                            s.getStatus().getLabel(),
                            s.getDiplomaNumber(),
                            s.getUniversityCertificateType().getCertificateType().getName(),
                            s.getCreatedAt()
                    ))
                    .collect(Collectors.toList());
            int start = (page - 1) * size;
            int end = Math.min(start + size, certificateResponseList.size());
            if (start >= certificateResponseList.size()) {
                return ApiResponseBuilder.success("Chưa có chứng chỉ nào!",certificateList);
            }

            List<CertificateResponse> pagedResult = certificateResponseList.subList(start, end);
            PaginatedData<CertificateResponse> data = new PaginatedData<>(pagedResult,
                    new PaginationMeta(certificateResponseList.size(), pagedResult.size(), size, page ,
                            (int) Math.ceil((double) certificateResponseList.size() / size)));

            return ApiResponseBuilder.success("Danh sách chứng chỉ",data);
        } catch (IllegalArgumentException e) {
            return ApiResponseBuilder.badRequest(e.getMessage());
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi" + e.getMessage());
        }
    }

    //chi tieets 1 chung chi
    @PreAuthorize("(hasAnyRole('ADMIN', 'PDT', 'KHOA','STUDENT')) and hasAuthority('READ')")
    @GetMapping("/certificate-detail/{id}")
    public ResponseEntity<?> getDetailCertificate(
            @PathVariable Long id
    ) {
        try {
            Certificate certificate= certificateService.findById(id);
            String ipfsUrl = certificate.getIpfsUrl() != null ? Constants.IPFS_URL + certificate.getIpfsUrl() : null;
            CertificateDetailResponse certificateDetailResponse = new CertificateDetailResponse(
                    certificate.getId(),
                    certificate.getStudent().getId(),
                    certificate.getStudent().getName(),
                    certificate.getStudent().getStudentClass().getName(),
                    certificate.getStudent().getStudentClass().getDepartment().getName(),
                    certificate.getStudent().getStudentClass().getDepartment().getUniversity().getName(),
                    certificate.getUniversityCertificateType().getCertificateType().getId(),
                    certificate.getUniversityCertificateType().getCertificateType().getName(),
                    certificate.getIssueDate(),
                    certificate.getDiplomaNumber(),
                    certificate.getStudent().getStudentCode(),
                    certificate.getStudent().getEmail(),
                    certificate.getStudent().getBirthDate(),
                    certificate.getStudent().getCourse(),
                    certificate.getGrantor(),
                    certificate.getSigner(),
                    certificate.getStatus().getLabel(),
                    certificate.getImageUrl(),
                    ipfsUrl,
                    certificate.getQrCodeUrl(),
                    certificate.getBlockchainTxHash(),
                    certificate.getUpdatedAt()
            );
            return ApiResponseBuilder.success("Chi tiết chứng chỉ", certificateDetailResponse);
        } catch (IllegalArgumentException e) {
            return ApiResponseBuilder.badRequest(e.getMessage());
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi " + e.getMessage());
        }
    }

    //---------------------------- PDT -------------------------------------------------------
    // all chunng chi cua 1 tr
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/list-certificates")
    public ResponseEntity<?> getCertificateOfUniversity(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String departmentName,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String studentCode,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String diplomaNumber
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            University university = universityService.getUniversityByEmail(username);
            Long universityId = university.getId();

            long totalItems = certificateService.countCertificatesOfUniversity(
                    universityId,
                    departmentName == null ? null : departmentName.trim(),
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim()
            );

            if (totalItems == 0) {
                PaginationMeta meta = new PaginationMeta(0, 0, size, page, 0);
                PaginatedData<CertificateResponse> data = new PaginatedData<>(Collections.emptyList(), meta);
                return ApiResponseBuilder.success("Không có chứng chỉ nào!", data);
            }

            int offset = (page - 1) * size;

            if (offset >= totalItems && totalItems > 0) {
                page = 1;
                offset = 0;
            }

            List<Certificate> certificates = certificateService.listCertificateOfUniversity(
                    universityId,
                    departmentName == null ? null : departmentName.trim(),
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim(),
                    size,
                    offset
            );

            List<CertificateResponse> certificateResponseList = certificates.stream().map(s -> new CertificateResponse(
                    s.getId(),
                    s.getStudent().getName(),
                    s.getStudent().getStudentClass().getName(),
                    s.getStudent().getStudentClass().getDepartment().getName(),
                    s.getIssueDate(),
                    s.getStatus().getLabel(),
                    s.getDiplomaNumber(),
                    s.getUniversityCertificateType().getCertificateType().getName(),
                    s.getCreatedAt()
            )).collect(Collectors.toList());

            int totalPages = (int) Math.ceil((double) totalItems / size);
            PaginationMeta meta = new PaginationMeta(totalItems, certificateResponseList.size(), size, page, totalPages);
            PaginatedData<CertificateResponse> data = new PaginatedData<>(certificateResponseList, meta);

            return ApiResponseBuilder.success("Danh sách chứng chỉ của trường", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    // danh sách chứng chỉ chưa xác nhận của 1 trường
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/list-certificates-pending")
    public ResponseEntity<?> getCertificateOfUniversityPending(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String departmentName,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String studentCode,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String diplomaNumber
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            University university = universityService.getUniversityByEmail(username);
            Long universityId = university.getId();

            long totalItems = certificateService.countCertificatesOfUniversityAndStatus(
                    universityId,
                    departmentName == null ? null : departmentName.trim(),
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim(),
                    Status.PENDING.name()
            );

            if (totalItems == 0) {
                PaginationMeta meta = new PaginationMeta(0, 0, size, page, 0);
                PaginatedData<CertificateResponse> data = new PaginatedData<>(Collections.emptyList(), meta);
                return ApiResponseBuilder.success("Không có chứng chỉ nào!", data);
            }

            int offset = (page - 1) * size;

            if (offset >= totalItems && totalItems > 0) {
                page = 1;
                offset = 0;
            }

            List<Certificate> certificates = certificateService.listCertificateOfUniversityAndStatus(
                    universityId,
                    departmentName == null ? null : departmentName.trim(),
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim(),
                    Status.PENDING.name(),
                    size,
                    offset
            );

            List<CertificateResponse> certificateResponseList = certificates.stream().map(s -> new CertificateResponse(
                    s.getId(),
                    s.getStudent().getName(),
                    s.getStudent().getStudentClass().getName(),
                    s.getStudent().getStudentClass().getDepartment().getName(),
                    s.getIssueDate(),
                    s.getStatus().getLabel(),
                    s.getDiplomaNumber(),
                    s.getUniversityCertificateType().getCertificateType().getName(),
                    s.getCreatedAt()
            )).collect(Collectors.toList());

            int totalPages = (int) Math.ceil((double) totalItems / size);
            PaginationMeta meta = new PaginationMeta(totalItems, certificateResponseList.size(), size, page, totalPages);
            PaginatedData<CertificateResponse> data = new PaginatedData<>(certificateResponseList, meta);

            return ApiResponseBuilder.success("Danh sách chứng chỉ chưa được xác nhận của trường", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    // danh sách chứng chỉ đã xác nhận của 1 trường
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/list-certificates-approved")
    public ResponseEntity<?> getCertificateOfUniversityApproved(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String departmentName,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String studentCode,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String diplomaNumber
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            University university = universityService.getUniversityByEmail(username);
            Long universityId = university.getId();

            long totalItems = certificateService.countCertificatesOfUniversityAndStatus(
                    universityId,
                    departmentName == null ? null : departmentName.trim(),
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim(),
                    Status.APPROVED.name()
            );

            if (totalItems == 0) {
                PaginationMeta meta = new PaginationMeta(0, 0, size, page, 0);
                PaginatedData<CertificateResponse> data = new PaginatedData<>(Collections.emptyList(), meta);
                return ApiResponseBuilder.success("Không có chứng chỉ nào!", data);
            }

            int offset = (page - 1) * size;

            if (offset >= totalItems && totalItems > 0) {
                page = 1;
                offset = 0;
            }

            List<Certificate> certificates = certificateService.listCertificateOfUniversityAndStatus(
                    universityId,
                    departmentName == null ? null : departmentName.trim(),
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim(),
                    Status.APPROVED.name(),
                    size,
                    offset
            );

            List<CertificateResponse> certificateResponseList = certificates.stream().map(s -> new CertificateResponse(
                    s.getId(),
                    s.getStudent().getName(),
                    s.getStudent().getStudentClass().getName(),
                    s.getStudent().getStudentClass().getDepartment().getName(),
                    s.getIssueDate(),
                    s.getStatus().getLabel(),
                    s.getDiplomaNumber(),
                    s.getUniversityCertificateType().getCertificateType().getName(),
                    s.getCreatedAt()
            )).collect(Collectors.toList());

            int totalPages = (int) Math.ceil((double) totalItems / size);
            PaginationMeta meta = new PaginationMeta(totalItems, certificateResponseList.size(), size, page, totalPages);
            PaginatedData<CertificateResponse> data = new PaginatedData<>(certificateResponseList, meta);

            return ApiResponseBuilder.success("Danh sách chứng chỉ đã được xác nhận của trường", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    // danh sách chứng chỉ bi từ chối xác nhận của 1 trường
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/list-certificates-rejected")
    public ResponseEntity<?> getCertificateOfUniversityRejected(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String departmentName,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String studentCode,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String diplomaNumber
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();

            University university = universityService.getUniversityByEmail(username);
            Long universityId = university.getId();

            long totalItems = certificateService.countCertificatesOfUniversityAndStatus(
                    universityId,
                    departmentName == null ? null : departmentName.trim(),
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim(),
                    Status.REJECTED.name()
            );

            if (totalItems == 0) {
                PaginationMeta meta = new PaginationMeta(0, 0, size, page, 0);
                PaginatedData<CertificateResponse> data = new PaginatedData<>(Collections.emptyList(), meta);
                return ApiResponseBuilder.success("Không có chứng chỉ nào!", data);
            }

            int offset = (page - 1) * size;

            if (offset >= totalItems && totalItems > 0) {
                page = 1;
                offset = 0;
            }

            List<Certificate> certificates = certificateService.listCertificateOfUniversityAndStatus(
                    universityId,
                    departmentName == null ? null : departmentName.trim(),
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim(),
                    Status.REJECTED.name(),
                    size,
                    offset
            );

            List<CertificateResponse> certificateResponseList = certificates.stream().map(s -> new CertificateResponse(
                    s.getId(),
                    s.getStudent().getName(),
                    s.getStudent().getStudentClass().getName(),
                    s.getStudent().getStudentClass().getDepartment().getName(),
                    s.getIssueDate(),
                    s.getStatus().getLabel(),
                    s.getDiplomaNumber(),
                    s.getUniversityCertificateType().getCertificateType().getName(),
                    s.getCreatedAt()
            )).collect(Collectors.toList());

            int totalPages = (int) Math.ceil((double) totalItems / size);
            PaginationMeta meta = new PaginationMeta(totalItems, certificateResponseList.size(), size, page, totalPages);
            PaginatedData<CertificateResponse> data = new PaginatedData<>(certificateResponseList, meta);

            return ApiResponseBuilder.success("Danh sách chứng chỉ bị từ chối xác nhận của trường", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    // xác nhận 1 chứng chỉ
    @PreAuthorize("hasAuthority('READ')")
    @PostMapping("/pdt/certificate-validation/{id}")
    public ResponseEntity<?> certificateValidation(@PathVariable("id") Long id){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            University university = universityService.getUniversityByEmail(username);
            Certificate certificate = certificateService.findByIdAndStatus(id, Status.APPROVED);

            if(certificate != null){
                return ApiResponseBuilder.badRequest("Chứng chỉ này đã được xác nhận rồi!");
            }
            certificateService.certificateValidation(university,id);
            return ApiResponseBuilder.success("Xác nhận thành công ", null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi " + e.getMessage());
        }
    }

    //xác nhận 1 list chứng chỉ
    @PreAuthorize("hasAuthority('READ')")
    @PostMapping("/pdt/confirm-certificate-list")
    public ResponseEntity<?> confirmCertificateList(@RequestBody ListValidationRequest request) {
        try {
            if (request == null || request.getIds() == null || request.getIds().isEmpty()) {
                return ApiResponseBuilder.badRequest("Vui lòng chọn chứng chỉ cần xác nhận!");
            }

            List<Long> ids = request.getIds();
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            University university = universityService.getUniversityByEmail(username);

            List<String> alreadyValidated = new ArrayList<>();

            for (Long id : ids) {
                Certificate certificate = certificateService.findByIdAndStatus(id,Status.APPROVED);
                if (certificate != null) {
                    alreadyValidated.add("Chứng chỉ ID " + id + " đã được xác nhận!");
                }
            }

            if (!alreadyValidated.isEmpty()) {
                return ApiResponseBuilder.listBadRequest(
                        "Không thể xác nhận vì có chứng chỉ đã được xác nhận.",
                        alreadyValidated
                );
            }

            certificateService.confirmCertificates(ids, university, httpServletRequest);
            return ApiResponseBuilder.success("Xác nhận list chứng chỉ thành công", null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    // xem chứng chỉ người dùng
    @GetMapping("/verify")
    public ResponseEntity<?> verifyCertificate(
            @RequestParam(required = false) String ipfsUrl,
            @RequestParam(required = false) String type
    ){
        try{
            if (ipfsUrl == null || ipfsUrl.trim().isEmpty() || type.trim().isEmpty()) {
                return ApiResponseBuilder.badRequest("Vui lòng nhập ipfs và type của chứng chỉ/văn bằng!");
            }
            switch (type.toLowerCase()) {
                case "degree":
                    Degree degree = degreeService.findByIpfsUrl(ipfsUrl);
                    if (degree == null)
                        return ApiResponseBuilder.badRequest("Không tìm thấy văn bằng!");
                    DegreeDetailResponse degreeDetailResponse = new DegreeDetailResponse();
                    degreeDetailResponse.setId(degree.getId());
                    degreeDetailResponse.setStudentId(degree.getStudent().getId());
                    degreeDetailResponse.setNameStudent(degree.getStudent().getName());
                    degreeDetailResponse.setClassName(degree.getStudent().getStudentClass().getName());
                    degreeDetailResponse.setDepartmentName(degree.getStudent().getStudentClass().getDepartment().getName());
                    degreeDetailResponse.setUniversity(degree.getStudent().getStudentClass().getDepartment().getUniversity().getName());
                    degreeDetailResponse.setStudentCode(degree.getStudent().getStudentCode());
                    degreeDetailResponse.setIssueDate(degree.getIssueDate());
                    degreeDetailResponse.setGraduationYear(degree.getGraduationYear());
                    degreeDetailResponse.setEmail(degree.getStudent().getEmail());
                    degreeDetailResponse.setBirthDate(degree.getStudent().getBirthDate());
                    degreeDetailResponse.setCourse(degree.getStudent().getCourse());
                    degreeDetailResponse.setSigner(degree.getSigner());
                    degreeDetailResponse.setStatus(degree.getStatus());
                    degreeDetailResponse.setImageUrl(degree.getImageUrl());
                    degreeDetailResponse.setIpfsUrl(Constants.IPFS_URL + degree.getIpfsUrl());
                    degreeDetailResponse.setQrCodeUrl(degree.getQrCode());
                    degreeDetailResponse.setTransactionHash(degree.getBlockchainTxHash());
                    degreeDetailResponse.setDiplomaNumber(degree.getDiplomaNumber());
                    degreeDetailResponse.setLotteryNumber(degree.getLotteryNumber());
                    degreeDetailResponse.setCreatedAt(degree.getUpdatedAt());
                    return ApiResponseBuilder.success("Chi tiết văn bằng", degreeDetailResponse);
                case "certificate":
                    Certificate certificate = certificateService.findByIpfsUrl(ipfsUrl);
                    if (certificate == null)
                        return ApiResponseBuilder.badRequest("Không tìm thấy chứng chỉ!");
                    CertificateDetailResponse certificateDetailResponse = new CertificateDetailResponse(
                            certificate.getId(),
                            certificate.getStudent().getId(),
                            certificate.getStudent().getName(),
                            certificate.getStudent().getStudentClass().getName(),
                            certificate.getStudent().getStudentClass().getDepartment().getName(),
                            certificate.getStudent().getStudentClass().getDepartment().getUniversity().getName(),
                            certificate.getUniversityCertificateType().getCertificateType().getId(),
                            certificate.getUniversityCertificateType().getCertificateType().getName(),
                            certificate.getIssueDate(),
                            certificate.getDiplomaNumber(),
                            certificate.getStudent().getStudentCode(),
                            certificate.getStudent().getEmail(),
                            certificate.getStudent().getBirthDate(),
                            certificate.getStudent().getCourse(),
                            certificate.getGrantor(),
                            certificate.getSigner(),
                            certificate.getStatus().getLabel(),
                            certificate.getImageUrl(),
                            Constants.IPFS_URL + ipfsUrl,
                            certificate.getQrCodeUrl(),
                            certificate.getBlockchainTxHash(),
                            certificate.getUpdatedAt()
                    );
                    return ApiResponseBuilder.success("Chi tiết chứng chỉ", certificateDetailResponse);
                default:
                    return ApiResponseBuilder.badRequest("Loại chứng chỉ/văn bằng không hợp lệ: degree hoặc certificate");
            }
        } catch (IllegalArgumentException e) {
            return ApiResponseBuilder.badRequest(e.getMessage());
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi" + e.getMessage());
        }
    }

    // xác nhan chung chi
    @PostMapping("/verify/decrypt")
    public ResponseEntity<?> decryptData(@RequestBody DecryptRequest request) {
        try {
            if (request == null || !StringUtils.hasText(request.getTransactionHash()) ||
                    !StringUtils.hasText(request.getPublicKeyBase64())) {
                return ApiResponseBuilder.badRequest("Vui lòng nhập đầy đủ thông tin!");
            }
            String encryptedData = blockChainService.extractEncryptedData(request.getTransactionHash());
            PublicKey publicKey = RSAKeyPairGenerator.getPublicKeyFromBase64(request.getPublicKeyBase64());
            String decrypted = rsaUtil.decryptWithPublicKeyFromHex(encryptedData, publicKey);
            Object jsonObject = objectMapper.readValue(decrypted, Object.class);
            return ApiResponseBuilder.success("Giải mã thành công", jsonObject);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Giải mã thất bại: " + e.getMessage());
        }
    }

    //---------------------------- KHOA -------------------------------------------------------
    // all chunng chi cua 1 khoa
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/khoa/list-certificates")
    public ResponseEntity<?> getCertificateOfDepartment(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String studentCode,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String diplomaNumber
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);
            Long departmentId = user.getDepartment().getId();

            long totalItems = certificateService.countCertificatesOfDepartment(
                    departmentId,
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim()
            );

            if (totalItems == 0) {
                PaginationMeta meta = new PaginationMeta(0, 0, size, page, 0);
                PaginatedData<CertificateResponse> data = new PaginatedData<>(Collections.emptyList(), meta);
                return ApiResponseBuilder.success("Không có chứng chỉ nào!", data);
            }

            int offset = (page - 1) * size;

            if (offset >= totalItems && totalItems > 0) {
                page = 1;
                offset = 0;
            }

            List<Certificate> certificates = certificateService.listCertificateOfDepartment(
                    departmentId,
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim(),
                    size,
                    offset
            );

            List<CertificateResponse> certificateResponseList = certificates.stream().map(s -> new CertificateResponse(
                    s.getId(),
                    s.getStudent().getName(),
                    s.getStudent().getStudentClass().getName(),
                    s.getStudent().getStudentClass().getDepartment().getName(),
                    s.getIssueDate(),
                    s.getStatus().getLabel(),
                    s.getDiplomaNumber(),
                    s.getUniversityCertificateType().getCertificateType().getName(),
                    s.getCreatedAt()
            )).collect(Collectors.toList());

            int totalPages = (int) Math.ceil((double) totalItems / size);
            PaginationMeta meta = new PaginationMeta(totalItems, certificateResponseList.size(), size, page, totalPages);
            PaginatedData<CertificateResponse> data = new PaginatedData<>(certificateResponseList, meta);

            return ApiResponseBuilder.success("Danh sách chứng chỉ của khoa", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    //chunng chi chưa xác nhận cua 1 khoa
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/khoa/list-certificates-pending")
    public ResponseEntity<?> getCertificateOfDepartmentPending(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String studentCode,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String diplomaNumber
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);
            Long departmentId = user.getDepartment().getId();

            long totalItems = certificateService.countCertificatesOfDepartmentOfStatus(
                    departmentId,
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim(),
                    Status.PENDING.name()
            );

            if (totalItems == 0) {
                PaginationMeta meta = new PaginationMeta(0, 0, size, page, 0);
                PaginatedData<CertificateResponse> data = new PaginatedData<>(Collections.emptyList(), meta);
                return ApiResponseBuilder.success("Không có chứng chỉ nào!", data);
            }

            int offset = (page - 1) * size;

            if (offset >= totalItems && totalItems > 0) {
                page = 1;
                offset = 0;
            }

            List<Certificate> certificates = certificateService.listCertificateOfDepartmentAndStatus(
                    departmentId,
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim(),
                    Status.PENDING.name(),
                    size,
                    offset
            );

            List<CertificateResponse> certificateResponseList = certificates.stream().map(s -> new CertificateResponse(
                    s.getId(),
                    s.getStudent().getName(),
                    s.getStudent().getStudentClass().getName(),
                    s.getStudent().getStudentClass().getDepartment().getName(),
                    s.getIssueDate(),
                    s.getStatus().getLabel(),
                    s.getDiplomaNumber(),
                    s.getUniversityCertificateType().getCertificateType().getName(),
                    s.getCreatedAt()
            )).collect(Collectors.toList());

            int totalPages = (int) Math.ceil((double) totalItems / size);
            PaginationMeta meta = new PaginationMeta(totalItems, certificateResponseList.size(), size, page, totalPages);
            PaginatedData<CertificateResponse> data = new PaginatedData<>(certificateResponseList, meta);

            return ApiResponseBuilder.success("Danh sách chứng chỉ chưa được xác nhận của khoa", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    //chunng chi bij từ chối xác nhận cua 1 khoa
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/khoa/list-certificates-rejected")
    public ResponseEntity<?> getCertificateOfDepartmentRejected(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String studentCode,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String diplomaNumber
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);
            Long departmentId = user.getDepartment().getId();

            long totalItems = certificateService.countCertificatesOfDepartmentOfStatus(
                    departmentId,
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim(),
                    Status.REJECTED.name()
            );

            if (totalItems == 0) {
                PaginationMeta meta = new PaginationMeta(0, 0, size, page, 0);
                PaginatedData<CertificateResponse> data = new PaginatedData<>(Collections.emptyList(), meta);
                return ApiResponseBuilder.success("Không có chứng chỉ nào!", data);
            }

            int offset = (page - 1) * size;

            if (offset >= totalItems && totalItems > 0) {
                page = 1;
                offset = 0;
            }

            List<Certificate> certificates = certificateService.listCertificateOfDepartmentAndStatus(
                    departmentId,
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim(),
                    Status.REJECTED.name(),
                    size,
                    offset
            );

            List<CertificateResponse> certificateResponseList = certificates.stream().map(s -> new CertificateResponse(
                    s.getId(),
                    s.getStudent().getName(),
                    s.getStudent().getStudentClass().getName(),
                    s.getStudent().getStudentClass().getDepartment().getName(),
                    s.getIssueDate(),
                    s.getStatus().getLabel(),
                    s.getDiplomaNumber(),
                    s.getUniversityCertificateType().getCertificateType().getName(),
                    s.getCreatedAt()
            )).collect(Collectors.toList());

            int totalPages = (int) Math.ceil((double) totalItems / size);
            PaginationMeta meta = new PaginationMeta(totalItems, certificateResponseList.size(), size, page, totalPages);
            PaginatedData<CertificateResponse> data = new PaginatedData<>(certificateResponseList, meta);

            return ApiResponseBuilder.success("Danh sách chứng chỉ bị từ chối của khoa", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    //chunng chi đã được xác nhận cua 1 khoa
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/khoa/list-certificates-approved")
    public ResponseEntity<?> getCertificateOfDepartmentApproved(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String studentCode,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String diplomaNumber
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);
            Long departmentId = user.getDepartment().getId();

            long totalItems = certificateService.countCertificatesOfDepartmentOfStatus(
                    departmentId,
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim(),
                    Status.APPROVED.name()
            );

            if (totalItems == 0) {
                PaginationMeta meta = new PaginationMeta(0, 0, size, page, 0);
                PaginatedData<CertificateResponse> data = new PaginatedData<>(Collections.emptyList(), meta);
                return ApiResponseBuilder.success("Không có chứng chỉ nào!", data);
            }

            int offset = (page - 1) * size;

            if (offset >= totalItems && totalItems > 0) {
                page = 1;
                offset = 0;
            }

            List<Certificate> certificates = certificateService.listCertificateOfDepartmentAndStatus(
                    departmentId,
                    className == null ? null : className.trim(),
                    studentCode == null ? null : studentCode.trim(),
                    studentName == null ? null : studentName.trim(),
                    diplomaNumber == null ? null : diplomaNumber.trim(),
                    Status.APPROVED.name(),
                    size,
                    offset
            );

            List<CertificateResponse> certificateResponseList = certificates.stream().map(s -> new CertificateResponse(
                    s.getId(),
                    s.getStudent().getName(),
                    s.getStudent().getStudentClass().getName(),
                    s.getStudent().getStudentClass().getDepartment().getName(),
                    s.getIssueDate(),
                    s.getStatus().getLabel(),
                    s.getDiplomaNumber(),
                    s.getUniversityCertificateType().getCertificateType().getName(),
                    s.getCreatedAt()
            )).collect(Collectors.toList());

            int totalPages = (int) Math.ceil((double) totalItems / size);
            PaginationMeta meta = new PaginationMeta(totalItems, certificateResponseList.size(), size, page, totalPages);
            PaginatedData<CertificateResponse> data = new PaginatedData<>(certificateResponseList, meta);

            return ApiResponseBuilder.success("Danh sách chứng chỉ đã được xác nhận của khoa", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    // tìm kiem sinh vien cho khoa để lấy id sinh viên -> cho thêm chứng chỉ
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/khoa/student-search")
    public ResponseEntity<?> searchStudent(
            @RequestParam(required = false) String studentCode
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username= authentication.getName();
            User user = userService.findByUser(username);
            List<Student> studentList = studentService.findByStudentOfDepartment(user.getDepartment().getId(),studentCode);

            List<StudentResponse> studentResponseList = studentList.stream()
                    .map(s -> new StudentResponse(
                            s.getId(),
                            s.getName(),
                            s.getStudentCode(),
                            s.getEmail(),
                            s.getStudentClass().getName(),
                            s.getBirthDate(),
                            s.getCourse()))
                    .collect(Collectors.toList());

            if(studentList.isEmpty()){
                return ApiResponseBuilder.success("Không tìm thấy sinh viên",studentList);
            }
            return ApiResponseBuilder.success("Tìm kiếm thành công", studentResponseList);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi");
        }
    }

    //tạo chung chi
    @PreAuthorize("hasAuthority('WRITE')")
    @PostMapping("/khoa/create-certificate")
    public ResponseEntity<?> createCertificate(
            @RequestParam("data") String dataJson,
            @RequestParam(required = false) Long studentId
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user =userService.findByUser(username);

            Student student = studentService.findById(studentId);
            if(student == null){
                return ApiResponseBuilder.badRequest("Không tìm thấy sinh viên!");
            }
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(dataJson);
            String diplomaNumber = jsonNode.get("diplomaNumber").asText();

            Long certificateTypeId = Long.valueOf(jsonNode.get("certificateTypeId").asText());
            Optional<Certificate> certificate = certificateService.existingStudentOfCertificate(studentId,certificateTypeId);
            if(!certificate.isEmpty()){
                return ApiResponseBuilder.badRequest("Sinh viên đã có loại chứng chỉ này");
            }
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate localDate = LocalDate.parse(jsonNode.get("issueDate").asText(), formatter);
                ZonedDateTime issueDate = localDate.atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh"));
                ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
                ZonedDateTime oneYearAgo = now.minusYears(1);
                ZonedDateTime oneYearLater = now.plusYears(1);

                if (issueDate.isBefore(oneYearAgo) || issueDate.isAfter(oneYearLater)) {
                    return ApiResponseBuilder.badRequest("Ngày cấp chứng chỉ phải trong vòng 1 năm trước và 1 năm sau kể từ hôm nay");
                }
            } catch (DateTimeParseException e) {
                return ApiResponseBuilder.badRequest("Ngày cấp chứng chỉ không đúng định dạng dd/MM/yyyy");
            }
            if(certificateService.existByDiplomaNumber(user.getUniversity().getId(),diplomaNumber.trim())){
                return ApiResponseBuilder.badRequest("Số hiệu bằng chứng chỉ này đã tồn tại!");
            }

            certificateService.createCertificate(student, jsonNode );
            return ApiResponseBuilder.success("Tạo chứng chỉ thành công, chờ PDT duyệt ", null);
        } catch (IllegalArgumentException e) {
            return ApiResponseBuilder.badRequest(e.getMessage());
        } catch (Exception e) {
            return ApiResponseBuilder.internalError(e.getMessage());
        }
    }

    //excel
    @PreAuthorize("hasAuthority('WRITE')")
    @PostMapping("/khoa/certificate/create-excel")
    public ResponseEntity<?> uploadExcel(
            @RequestParam("file") MultipartFile file,
            @RequestParam("certificateTypeId") Long certificateTypeId) throws IOException {
        if(file.isEmpty()){
            return ApiResponseBuilder.badRequest("Vui lòng chọn file excel để thêm chứng chỉ!");
        }
        String contentType = file.getContentType();
        String fileName = file.getOriginalFilename();

        if (!("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType)
                || "application/vnd.ms-excel".equals(contentType))
                || fileName == null
                || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            return ApiResponseBuilder.badRequest("File không đúng định dạng Excel (.xlsx hoặc .xls)");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username= authentication.getName();
        User user = userService.findByUser(username);

        CertificateType certificateType= certificateTypeService.findById(certificateTypeId);
        UniversityCertificateType universityCertificateType =
                universityCertificateTypeService.findByCartificateType(certificateType);

        EasyExcel.read(
                file.getInputStream(),
                CertificateExcelRowDTO.class,
                new CertificateExcelListener(
                        studentService,
                        user.getDepartment().getId(),
                        universityCertificateType,
                        certificateService,
                        graphicsTextWriter,
                        auditLogService,
                        logRepository,
                        httpServletRequest
                )
        ).sheet().doRead();

        return ApiResponseBuilder.success("Tạo chứng chỉ thành công" , null);
    }

    // sửa chung chi
    @PreAuthorize("hasAuthority('READ')")
    @PutMapping("/khoa/update-certificate/{id}")
    public ResponseEntity<?> updateCertificateType(
            @PathVariable("id")  Long id,
            @RequestBody CertificateRequest request)
    {
        try {
            if (request == null
                    || !StringUtils.hasText(request.getIssueDate())
                    || !StringUtils.hasText(request.getDiplomaNumber())
                    || !StringUtils.hasText(request.getGrantor())
                    || !StringUtils.hasText(request.getSigner())) {
                return ApiResponseBuilder.badRequest("Vui lòng nhập đầy đủ thông tin!");
            }
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate localDate = LocalDate.parse(request.getIssueDate(), formatter);
                ZonedDateTime issueDate = localDate.atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh"));
                ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
                ZonedDateTime oneYearAgo = now.minusYears(1);
                ZonedDateTime oneYearLater = now.plusYears(1);

                if (issueDate.isBefore(oneYearAgo) || issueDate.isAfter(oneYearLater)) {
                    return ApiResponseBuilder.badRequest("Ngày cấp chứng chỉ phải trong vòng 1 năm trước và 1 năm sau kể từ hôm nay");
                }
            } catch (DateTimeParseException e) {
                return ApiResponseBuilder.badRequest("Ngày cấp chứng chỉ không đúng định dạng dd/MM/yyyy");
            }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user =userService.findByUser(username);
            if(certificateService.existByDiplomaNumberIgnoreId(user.getUniversity().getId(),request.getDiplomaNumber().trim(),id)){
                return ApiResponseBuilder.badRequest("Số hiệu bằng chứng chỉ này đã tồn tại!");
            }

            if(certificateService.update(id, request)){
                return ApiResponseBuilder.success("Sửa thông tin loại chứng chỉ thành công", null);
            }
            return ApiResponseBuilder.badRequest("Sửa thông tin loại chứng chỉ thát bại!");
        } catch (Exception e) {
            return ApiResponseBuilder.badRequest(e.getMessage());
        }
    }

    // từ chối 1 chứng chỉ
    @PreAuthorize("hasAuthority('READ')")
    @PostMapping("/pdt/certificate-rejected/{id}")
    public ResponseEntity<?> certificateRejected(@PathVariable("id") Long id){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);
            Certificate certificate = certificateService.findByIdAndStatus(id, Status.APPROVED);

            if(certificate != null){
                return ApiResponseBuilder.badRequest("Chứng chỉ này đã được xác nhận rồi!");
            }
            certificateService.certificateRejected(id, user);
            return ApiResponseBuilder.success("Từ chối xác nhận thành công ", null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi " + e.getMessage());
        }
    }

    //từ chối xác nhận 1 list chứng chỉ
    @PreAuthorize("hasAuthority('READ')")
    @PostMapping("/pdt/reject-a-list-of-certificate")
    public ResponseEntity<?> rejectAListOfCertificate(@RequestBody ListValidationRequest request) {
        try {
            if (request == null || request.getIds() == null || request.getIds().isEmpty()) {
                return ApiResponseBuilder.badRequest("Vui lòng chọn chứng chỉ cần từ chối xác nhận!");
            }
            List<Long> ids = request.getIds();
            List<String> alreadyValidated = new ArrayList<>();

            for (Long id : ids) {
                Certificate certificate = certificateService.findByIdAndStatus(id,Status.APPROVED);
                if (certificate != null) {
                    alreadyValidated.add("Chứng chỉ ID " + id + " đã được xác nhận!");
                }
            }

            if (!alreadyValidated.isEmpty()) {
                return ApiResponseBuilder.listBadRequest(
                        "Không thể từ chối xác nhận vì có chứng chỉ đã được xác nhận.",
                        alreadyValidated
                );
            }
            certificateService.rejectCertificates(ids);
            return ApiResponseBuilder.success("Từ chối xác nhận danh sách chứng chỉ thành công", null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    //danh sách ch ch cua sinh vien
    @GetMapping("/student/certificate-list")
    public ResponseEntity<?> rejectAListOfStudent(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String diplomaNumber
    ){
        try {
            if (page < 1) page = 1;
            if (size < 1) size = 10;
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Student student = studentService.findByEmail(username);

            long totalItems = certificateService.countCertificateOfStudent(
                    student.getId(),
                    diplomaNumber == null ? null : diplomaNumber.trim()
            );
            if (totalItems == 0) {
                PaginationMeta meta = new PaginationMeta(0, 0, size, page, 0);
                PaginatedData<CertificateResponse> data = new PaginatedData<>(Collections.emptyList(), meta);
                return ApiResponseBuilder.success("Không có chứng chỉ nào!", data);
            }
            int offset = (page - 1) * size;
            if (offset >= totalItems && totalItems > 0) {
                page = 1;
                offset = 0;
            }
            int totalPages = (int) Math.ceil((double) totalItems / size);
            List<CertificateOfStudentResponse> certificateResponseList = certificateService.certificateOfStudent(student.getId(),diplomaNumber,size,offset);
            PaginationMeta meta = new PaginationMeta(totalItems, certificateResponseList.size(), size, page, totalPages);
            PaginatedData<CertificateOfStudentResponse> data = new PaginatedData<>(certificateResponseList, meta);

            return ApiResponseBuilder.success("Danh sách chứng chỉ của sinh viên", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/pdt/export-certificates")
    public void exportCertificates(
            @RequestParam(name = "type", required = false) String type,
            HttpServletResponse response) throws IOException
    {
        String status = null;
        if (type != null) {
            try {
                status = type;
            } catch (IllegalArgumentException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Type không đúng định dạng!");
                return;
            }
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("UTF-8");

        String fileName = URLEncoder.encode("chung_chi_" + (type == null ? "all" : type) , StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");

        List<CertificateExcelDTO> data = certificateService.getAllCertificateDTOs(status);

        EasyExcel.write(response.getOutputStream(), CertificateExcelDTO.class)
                .registerWriteHandler(ExcelStyleUtil.certificateStyleStrategy())
                .sheet("Danh sách chứng chỉ " + type)
                .doWrite(data);
    }

}
