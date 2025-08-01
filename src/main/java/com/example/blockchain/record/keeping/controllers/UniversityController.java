package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.dtos.request.ChangePasswordRequest;
import com.example.blockchain.record.keeping.dtos.request.UniversityRequest;
import com.example.blockchain.record.keeping.enums.ActionType;
import com.example.blockchain.record.keeping.enums.Entity;
import com.example.blockchain.record.keeping.enums.LogTemplate;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.ActionChangeRepository;
import com.example.blockchain.record.keeping.repositorys.LogRepository;
import com.example.blockchain.record.keeping.response.*;
import com.example.blockchain.record.keeping.services.*;
import com.example.blockchain.record.keeping.utils.EnvUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UniversityController {
    private final PasswordEncoder passwordEncoder;
    private final UniversityService universityService;
    private final UserService userService;
    private final ImageUploadService imageUploadService;
    private final ActionChangeRepository actionChangeRepository;
    private final LogRepository logRepository;
    private final AuditLogService auditLogService;
    private final HttpServletRequest httpServletRequest;
    private final STUcoinService stUcoinService;


    //---------------------------- ADMIN -------------------------------------------------------
    //danh sách university
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/admin/list-university")
    public ResponseEntity<?> getAllUniversity(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String nameUniversity
    ){
        try{
            List<User> userList = universityService.findAllUserUniversity(nameUniversity);
            List<UniversityResponse> universityReponsesList = userList.stream()
                    .map(u-> new UniversityResponse(
                            u.getId(),
                            u.getUniversity().getName(),
                            u.getUniversity().getEmail(),
                            u.getUniversity().getAddress(),
                            u.getUniversity().getTaxCode(),
                            u.getUniversity().getWebsite(),
                            u.getUniversity().getLogo(),
                            u.isLocked()
                    ))
                    .collect(Collectors.toList());

            if ((nameUniversity != null && !nameUniversity.isEmpty())){
                if(universityReponsesList.isEmpty()){
                    return ApiResponseBuilder.success("Không tìm thấy!",universityReponsesList);
                }
            }

            if (page < 1) page = 1;
            if (size < 1) size = 10;

            int start = (page - 1)* size;
            int end = Math.min(start + size, universityReponsesList.size());
            if (start >= universityReponsesList.size()) {
                return ApiResponseBuilder.success("Chưa có trường nào!",universityReponsesList);
            }
            List<UniversityResponse> pagedResult = universityReponsesList.subList(start, end);
            PaginatedData<UniversityResponse> data = new PaginatedData<>(pagedResult,
                    new PaginationMeta(
                            universityReponsesList.size(),
                            pagedResult.size(),
                            size,
                            page,
                            (int) Math.ceil((double) universityReponsesList.size() / size)));

            return ApiResponseBuilder.success("Lấy danh sách các trường đại học thành công", data);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi!");
        }
    }

    //chi tiết university
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/admin/detail-university/{id}")
    public ResponseEntity<?> getDetailUniversity(@PathVariable("id")  Long id)
    {
        User user = userService.finbById(id);
        UniversityDetailResponse data =new UniversityDetailResponse(
                user.getUniversity().getName(),
                user.getUniversity().getEmail(),
                user.getUniversity().getAddress(),
                user.getUniversity().getTaxCode(),
                user.getUniversity().getWebsite(),
                user.getUniversity().getLogo(),
                user.isLocked(),
                user.isVerified(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
        return ApiResponseBuilder.success("Thông tin chi tiết của trường", data);
    }

    //update tài khoản university
    @PreAuthorize("@permissionService.hasPermission(authentication, 'UPDATE')")
    @PutMapping("/pdt/update")
    public ResponseEntity<?> updateUniversityInfo(
            @Valid @ModelAttribute UniversityRequest universityRequest,
            BindingResult bindingResult
    ) {
        try {
            if (bindingResult.hasErrors()) {
                String errorMessage = bindingResult.getFieldErrors().stream()
                        .map(err -> err.getDefaultMessage())
                        .collect(Collectors.joining(", "));
                return ApiResponseBuilder.badRequest(errorMessage);
            }

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            University university = universityService.getUniversityByEmail(username);

            if (university == null) {
                return ApiResponseBuilder.notFound("Không tìm thấy tài khoản phòng đào tạo");
            }

            if (universityService.existsByNameIgnoreCaseAndIdNot(universityRequest.getName(), university.getId())) {
                return ApiResponseBuilder.badRequest("Tên trường đã tồn tại!");
            }

            if (!universityRequest.getEmail().equals(university.getEmail())
                    && userService.isEmailRegistered(universityRequest.getEmail())) {
                return ApiResponseBuilder.badRequest("Email này đã tồn tại!");
            }
            universityService.update(university, universityRequest);
            return ApiResponseBuilder.success("Cập nhật thông tin thành công", null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    //logo
    @PreAuthorize("@permissionService.hasPermission(authentication, 'UPDATE')")
    @PutMapping("/pdt/university/logo")
    public ResponseEntity<?> updateLogo(@RequestParam("logo") MultipartFile logo) {
        try {
            if (logo.isEmpty() || !logo.getContentType().startsWith("image/")) {
                return ApiResponseBuilder.badRequest("Logo tải lên không hợp lệ!");
            }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            University university = universityService.getUniversityByEmail(userName);

            University universityOld = auditLogService.cloneLogoUniversity(university);

            String imageUrl = imageUploadService.uploadImage(logo);

            university.setLogo(imageUrl);
            universityService.save(university);
            ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

            String ipAdress = auditLogService.getClientIp(httpServletRequest);
            List<ActionChange> changes = auditLogService.compareObjects(null, universityOld, university);
            if (!changes.isEmpty()) {
                Log log = new Log();
                log.setUser(auditLogService.getCurrentUser());
                log.setActionType(ActionType.UPDATED);
                log.setEntityName(Entity.universitys);
                log.setEntityId(null);
                log.setDescription(LogTemplate.UPDATE_UNIVERCITY_LOGO.getName());
                log.setIpAddress(ipAdress);
                log.setCreatedAt(vietnamTime.toLocalDateTime());

                log = logRepository.save(log);

                for (ActionChange change : changes) {
                    change.setLog(log);
                }
                actionChangeRepository.saveAll(changes);
            }
            return ApiResponseBuilder.success("Cập nhật logo thành công", null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi khi cập nhật logo: " + e.getMessage());
        }
    }

    @PreAuthorize("@permissionService.hasPermission(authentication, 'UPDATE')")
    @PutMapping("/pdt/university/seal")
    public ResponseEntity<?> updateSeal(@RequestParam("seal") MultipartFile seal) {
        try {
            if (seal.isEmpty() || !seal.getContentType().startsWith("image/")) {
                return ApiResponseBuilder.badRequest("Dấu mộc tải lên không hợp lệ!");
            }
            ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userName = authentication.getName();
            University university = universityService.getUniversityByEmail(userName);

            University universityOld = auditLogService.cloneSelaUniversity(university);

            String imageUrl = imageUploadService.uploadImage(seal);
            university.setSealImageUrl(imageUrl);
            universityService.save(university);

            String ipAdress = auditLogService.getClientIp(httpServletRequest);
            List<ActionChange> changes = auditLogService.compareObjects(null, universityOld, university);
            if (!changes.isEmpty()) {
                Log log = new Log();
                log.setUser(auditLogService.getCurrentUser());
                log.setActionType(ActionType.UPDATED);
                log.setEntityName(Entity.universitys);
                log.setEntityId(null);
                log.setDescription(LogTemplate.UPDATE_UNIVERCITY_SEAL.getName());
                log.setIpAddress(ipAdress);
                log.setCreatedAt(vietnamTime.toLocalDateTime());

                log = logRepository.save(log);

                for (ActionChange change : changes) {
                    change.setLog(log);
                }
                actionChangeRepository.saveAll(changes);
            }

            return ApiResponseBuilder.success("Cập nhật dấu mộc thành công", null);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi khi cập nhật dấu mộc: " + e.getMessage());
        }
    }

    //tạo thêm coin
    @PreAuthorize("@permissionService.hasPermission(authentication, 'CREATE')")
    @PostMapping("/pdt/tokens/mint")
    public ResponseEntity<?> mintToken(
            @RequestParam String amount
    ) {
        try {
            String toAddress = EnvUtil.get("METAMASK_ADDRESS");

            if (amount == null || amount.isBlank()) {
                return ApiResponseBuilder.badRequest("Vui lòng nhập số lượng token");
            }
            BigInteger amountBI;
            BigDecimal rawDecimal;
            try {
                rawDecimal = new BigDecimal(amount);
                if (rawDecimal.compareTo(BigDecimal.ZERO) <= 0) {
                    return ApiResponseBuilder.badRequest("Số lượng phải lớn hơn 0");
                }
                amountBI = rawDecimal.multiply(BigDecimal.TEN.pow(18)).toBigIntegerExact();
            } catch (NumberFormatException | ArithmeticException e) {
                return ApiResponseBuilder.badRequest("Số lượng token không hợp lệ");
            }

            TransactionReceipt receipt = stUcoinService.mint(toAddress, amountBI);

            return ApiResponseBuilder.success("Tạo token thành công", null);

        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi khi mint token: " + e.getMessage());
        }
    }
}
