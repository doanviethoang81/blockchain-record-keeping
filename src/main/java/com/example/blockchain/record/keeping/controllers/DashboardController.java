package com.example.blockchain.record.keeping.controllers;

import com.example.blockchain.record.keeping.dtos.*;
import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.models.User;
import com.example.blockchain.record.keeping.response.*;
import com.example.blockchain.record.keeping.services.CertificateService;
import com.example.blockchain.record.keeping.services.DegreeService;
import com.example.blockchain.record.keeping.services.UniversityService;
import com.example.blockchain.record.keeping.services.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final CertificateService certificateService;

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

    // thống kê sl văn bằng chứng chỉ theo từng khoa pdt
    @GetMapping("/pdt/dashboard/faculty-degree-statistics")
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

    // thống kê sl văn bằng theo xêp loại của 1 trường
    @GetMapping("/pdt/dashboard/degree-rating-statistics")
    public ResponseEntity<?> getDegreeClassificationStatisticsOfUniversity() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            University university = universityService.getUniversityByEmail(username);

            DegreeClassificationStatisticsResponse result = degreeService.degreeClassificationStatisticsOfUniversity(university.getId());
            return ApiResponseBuilder.success("Thống kê văn bằng của 1 trường theo xếp loại thành công", result);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    // thống kê sl văn bằng theo xêp loại của 1 khoa
    @GetMapping("/khoa/dashboard/degree-rating-statistics")
    public ResponseEntity<?> getDegreeClassificationStatisticsOfDepartment() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);

            DegreeClassificationStatisticsResponse result = degreeService.degreeClassificationStatisticsOfDepartment(user.getDepartment().getId());
            return ApiResponseBuilder.success("Thống kê văn bằng của 1 khoa theo xếp loại thành công", result);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }


    // thống kê sl chứng chỉ theo các tháng của 1 tr
    @GetMapping("/pdt/dashboard/monthly-certificate-statistics")
    public ResponseEntity<?> monthlyCertificateStatisticsOfUniversity() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            University university = universityService.getUniversityByEmail(username);

            List<MonthlyCertificateStatisticsResponse> result = certificateService.monthlyCertificateStatisticsOfUniversity(university.getId());
            return ApiResponseBuilder.success("Thống kê chứng chỉ của trường theo các tháng", result);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    // thống kê sl chứng chỉ theo các tháng của 1 khoa
    @GetMapping("/khoa/dashboard/monthly-certificate-statistics")
    public ResponseEntity<?> monthlyCertificateStatisticsOfDepartment() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);

            List<MonthlyCertificateStatisticsResponse> result = certificateService.monthlyCertificateStatisticsOfDepartment(user.getDepartment().getId());
            return ApiResponseBuilder.success("Thống kê chứng chỉ của khoa theo các tháng", result);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    // thống kê số lượng văn bằng trong 5 năm theo trường
    @GetMapping("/pdt/dashboard/degree-statistics-by-year")
    public ResponseEntity<?> getDegreeClassificationByUniversityAndLast5Years() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);

            List<DegreeClassificationByYearResponse> result = degreeService.getDegreeClassificationByUniversityAndLast5Years(user.getUniversity().getId());
            return ApiResponseBuilder.success("Thống kê văn bằng trong năm 5 của trường", result);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    // thống kê số lượng văn bằng trong 5 năm theo khoa
    @GetMapping("/khoa/dashboard/degree-statistics-by-year")
    public ResponseEntity<?> getDegreeClassificationByDepartmentAndLast5Years() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);

            List<DegreeClassificationByYearResponse> result = degreeService.getDegreeClassificationByDepartmentAndLast5Years(user.getDepartment().getId());
            return ApiResponseBuilder.success("Thống kê văn bằng trong năm 5 của khoa", result);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    // thống kê số lượng chứng chỉ trong 5 năm của tr
    @GetMapping("/pdt/dashboard/certificate-statistics-by-year")
    public ResponseEntity<?> getCertificateClassificationByUniversityAndLast5Years() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);

            List<DegreeClassificationByYearResponse> result = certificateService.getCertificateClassificationByUniversityAndLast5Years(user.getUniversity().getId());
            return ApiResponseBuilder.success("Thống kê chứng chỉ bằng trong năm 5 của trường", result);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    // thống kê số lượng chứng chỉ trong 5 năm của khoa
    @GetMapping("/khoa/dashboard/certificate-statistics-by-year")
    public ResponseEntity<?> getCertificateClassificationByDepartmentAndLast5Years() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);

            List<DegreeClassificationByYearResponse> result = certificateService.getCertificateClassificationByDepartmentAndLast5Years(user.getDepartment().getId());
            return ApiResponseBuilder.success("Thống kê chứng chỉ trong năm 5 của khoa", result);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    // thống kê chứng chỉ đã cấp của từng loại ch ch trong 1 tr
    @GetMapping("/pdt/dashboard/count-certificate-type")
    public ResponseEntity<?> getTheNumberOfEachTypeOfCertificateInTheUniversity() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);
            List<CountCertificateTypeResponse> result = certificateService.countCertificateTypeOfUniversity(user.getUniversity().getId());
            return ApiResponseBuilder.success("Thống kê số lượng từng loại chứng chỉ đã cấp của trường", result);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    // thống kê chứng chỉ đã cấp của từng loại ch ch trong 1 khoa
    @GetMapping("/khoa/dashboard/count-certificate-type")
    public ResponseEntity<?> getTheNumberOfEachTypeOfCertificateInTheDepartment() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUser(username);
            List<CountCertificateTypeResponse> result = certificateService.countCertificateTypeOfDepartment(user.getDepartment().getId());
            return ApiResponseBuilder.success("Thống kê số lượng từng loại chứng chỉ đã cấp của khoa", result);
        } catch (Exception e) {
            return ApiResponseBuilder.internalError("Lỗi: " + e.getMessage());
        }
    }

    //top khoa, lớp chch/vb
    @GetMapping("/pdt/statistics")
    public ResponseEntity<?> getUniversityStatistics() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username= authentication.getName();
        University university = universityService.getUniversityByEmail(username);
        StatisticsSummaryDTO statisticsSummaryDTO = certificateService.getUniversityStatistics(university.getId());
        return ApiResponseBuilder.success("Lấy thông tin thành công",statisticsSummaryDTO);
    }

    //top khoa, lớp chch/vb
    @GetMapping("/khoa/statistics")
    public ResponseEntity<?> getUniversityStatisticsOfDepartment() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username= authentication.getName();
        User user = userService.findByUser(username);
        StatisticsSummaryOfDepartmentDTO statisticsSummaryDTO = certificateService.getDeparmentStatistics(user.getDepartment().getId());
        return ApiResponseBuilder.success("Lấy thông tin thành công",statisticsSummaryDTO);
    }
}
