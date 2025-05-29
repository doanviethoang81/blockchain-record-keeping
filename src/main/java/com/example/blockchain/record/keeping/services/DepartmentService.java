package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.models.User;
import com.example.blockchain.record.keeping.models.UserPermission;
import com.example.blockchain.record.keeping.repositorys.DepartmentRepository;
import com.example.blockchain.record.keeping.repositorys.UserRepository;
import com.example.blockchain.record.keeping.response.ApiResponseBuilder;
import com.example.blockchain.record.keeping.response.UserReponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService  implements IDepartmentService{

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    @Override
    public Department save(Department department) {
        return departmentRepository.save(department);
    }

    @Override
    public boolean existsByNameAndUniversity(String name, Long universityId) {
        return departmentRepository.existsByNameIgnoreCaseAndUniversityIdAndStatus(name.trim(), universityId, Status.ACTIVE);
    }

    @Override
    public List<Department> listDepartmentOfUniversity(University university) {
        return departmentRepository.findByUniversity(university);
    }

    @Override
    public Department updateDepartment(Long id, String name, String email) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        Department department = departmentRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy khóa có id " + id));
        department.setName(name);
        department.setUpdatedAt(vietnamTime.toLocalDateTime());


        User user = userRepository.findByDepartment(department)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy User với id "+ id));

        user.setEmail(email);
        user.setUpdatedAt(vietnamTime.toLocalDateTime());
        userRepository.save(user);

        return departmentRepository.save(department);
    }


    @Override
    public Department deleteDepartment(Long id) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        Department departmentIsDelete = departmentRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy khóa có id " + id));
        departmentIsDelete.setStatus(Status.DELETED);
        departmentIsDelete.setUpdatedAt(vietnamTime.toLocalDateTime());

        User user = userRepository.findByDepartment(departmentIsDelete)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy thông tin khoa"));
        userRepository.delete(user);// xem lại nếu lỗi
        return departmentRepository.save(departmentIsDelete);
    }

    @Override
    public Department findById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy khoa có id "+ id));
    }

    @Override
    public Optional<Department> findByDepartmentNameOfUniversity(Long universityId, String name) {
        return departmentRepository.findByDepartmentNameOfUniversity(universityId,name);
    }

}
