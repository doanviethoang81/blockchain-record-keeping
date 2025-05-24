package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.response.*;
import com.example.blockchain.record.keeping.services.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
    private final DepartmentService departmentService;
    private final RoleService roleService;
    private final PermissionService permissionService;
    private final UserPermissionService userPermissionService;

    //---------------------------- ADMIN -------------------------------------------------------


    //các khoa của trường đại học
    @PreAuthorize("hasAuthority('READ')")
    @GetMapping("/pdt/list-department-of-university")
    public ResponseEntity<?> getListUserOfUniversity(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            University university = universityService.getUniversityByEmail(username);

            User currentUser = userService.findByUser(username);
            List<User> listUser = userService.listUser(university).stream()
                    .filter(user -> user.getDepartment() != null) // chỉ lấy user có khoa
                    .filter(user -> !user.getId().equals(currentUser.getId())) // loại bỏ user đang đăng nhập
                    .filter(user -> user.getDepartment().getStatus() != Status.DELETED)
                    .collect(Collectors.toList());
            List<UserReponse> userReponses = new ArrayList<>();

            for (User user : listUser) {
                List<UserPermission> userPermissions = userPermissionService.listUserPermissions(user);

                List<String> permissions = userPermissions.stream()
                        .map(up -> up.getPermission().getAction())
                        .collect(Collectors.toList());

                UserReponse userReponse = new UserReponse(
                        user.getId(),
                        user.getDepartment().getName(),
                        user.getEmail(),
                        user.isLocked(),
                        permissions
                );
                userReponses.add(userReponse);
            }

            int start = page * size;
            int end = Math.min(start + size, userReponses.size());
            if (start >= userReponses.size()) {
                return ApiResponseBuilder.success("Chưa có khoa nào", null);
            }

            List<UserReponse> pagedResult = userReponses.subList(start, end);
            PaginatedData<UserReponse> data = new PaginatedData<>(pagedResult,
                    new PaginationMeta(userReponses.size(), pagedResult.size(), size, page + 1,
                            (int) Math.ceil((double) userReponses.size() / size)));

            return ApiResponseBuilder.success(
                    "Lấy danh sách các khoa của trường thành công.",data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
