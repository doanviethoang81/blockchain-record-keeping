package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.dtos.StatisticsAdminDTO;
import com.example.blockchain.record.keeping.dtos.StatisticsDepartmentDTO;
import com.example.blockchain.record.keeping.dtos.StatisticsUniversityDTO;
import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.models.User;
import com.example.blockchain.record.keeping.response.ApiResponseBuilder;
import com.example.blockchain.record.keeping.response.FacultyDegreeStatisticResponse;
import com.example.blockchain.record.keeping.services.DegreeService;
import com.example.blockchain.record.keeping.services.UniversityService;
import com.example.blockchain.record.keeping.services.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.prefix:/api/v1}")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final UserService userService;
    private final UniversityService universityService;
    private final DegreeService degreeService;

    //---------------------------- ADMIN -------------------------------------------------------

    //    Dashboard admin theem truong bi khoa
    @GetMapping("/admin/dashboard")
    public ResponseEntity<?> getDashboardAdmin(){
        try{
            StatisticsAdminDTO statisticsAdminDTO = userService.dashboardAdmin();
            return ApiResponseBuilder.success("Lấy thông tin dashboard admin thành công", statisticsAdminDTO);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi " + e.getMessage());
        }
    }

    //---------------------------- PDT -------------------------------------------------------

    //    Dashboard pdt
    @GetMapping("/pdt/dashboard")
    public ResponseEntity<?> getDashboardUniversity(){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            University university = universityService.getUniversityByEmail(username);

            StatisticsUniversityDTO statisticsUniversityDTO = userService.dashboardUniversity(university.getId());
            return ApiResponseBuilder.success("Lấy thông tin dashboard university thành công", statisticsUniversityDTO);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi " + e.getMessage());
        }
    }

    //---------------------------- KHOA -------------------------------------------------------
    //    Dashboard khoa
    @GetMapping("/khoa/dashboard")
    public ResponseEntity<?> getDashboardDepartment(){
        try{
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);

            StatisticsDepartmentDTO statisticsDepartmentDTO = userService.dashboarDepartment(user.getDepartment().getId());
            return ApiResponseBuilder.success("Lấy thông tin dashboard khoa thành công", statisticsDepartmentDTO);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi " + e.getMessage());
        }
    }

    // thống kê sl văn bằng theo từng khoa pdt (hỏi lại có thêm cả ch ch th sua lai
    @GetMapping("/pdt/faculty-degree-statistics")
    public ResponseEntity<?> getFacultyDegreeStatistics() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            University university = universityService.getUniversityByEmail(username);

            List<FacultyDegreeStatisticResponse> statistics = degreeService.getFacultyDegreeStatistics(university.getId());

            return ApiResponseBuilder.success("Thống kê thành công", statistics);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

}
