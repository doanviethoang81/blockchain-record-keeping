package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.StatisticsAdminDTO;
import com.example.blockchain.record.keeping.dtos.StatisticsDepartmentDTO;
import com.example.blockchain.record.keeping.dtos.StatisticsUniversityDTO;
import com.example.blockchain.record.keeping.dtos.request.ChangePasswordDepartmentRequest;
import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.models.User;

import java.util.List;
import java.util.Optional;

public interface IUserService {
    User findByUser(String email);
    User save(User user);
    List<User> listDepartmentOfUniversity(Long universityId, String name);
    boolean isEmailRegistered(String email);
    boolean existsById(Long id);

    boolean updateLocked(Long userId);

    User finbById(Long id);

    boolean changePasswordDepartment(Long id,ChangePasswordDepartmentRequest changePasswordDepartmentRequest);

    boolean togglePermission(Long userId, String active);

    User findByUniversity(University university);

    StatisticsAdminDTO dashboardAdmin();
    StatisticsUniversityDTO dashboardUniversity(Long universityId);
    StatisticsDepartmentDTO dashboarDepartment(Long departmentId);

    User findByDepartment(Department department);
}
