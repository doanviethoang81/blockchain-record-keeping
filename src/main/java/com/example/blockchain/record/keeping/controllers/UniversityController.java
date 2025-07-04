package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.response.*;
import com.example.blockchain.record.keeping.services.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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



}
