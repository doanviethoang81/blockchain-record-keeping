package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.annotation.Auditable;
import com.example.blockchain.record.keeping.enums.ActionType;
import com.example.blockchain.record.keeping.enums.Entity;
import com.example.blockchain.record.keeping.enums.LogTemplate;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.ActionChange;
import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.Log;
import com.example.blockchain.record.keeping.models.StudentClass;
import com.example.blockchain.record.keeping.repositorys.ActionChangeRepository;
import com.example.blockchain.record.keeping.repositorys.LogRepository;
import com.example.blockchain.record.keeping.repositorys.StudentClassRepository;
import com.example.blockchain.record.keeping.response.StudentClassResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentClassService implements IStudentClassService{

    private final StudentClassRepository studentClassRepository;
    private final AuditLogService auditLogService;
    private final HttpServletRequest httpServletRequest;
    private final LogRepository logRepository;
    private final ActionChangeRepository actionChangeRepository;

    @Override
    public StudentClass findById(Long id) {
        return studentClassRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy lớp có id "+ id));
    }

    @Override
    public StudentClass findByName(String name) {
        return studentClassRepository.findByName(name)
                .orElseThrow(()-> new RuntimeException("Không tìm thấy lớp có tên "+ name));
    }

    @Override
    public List<StudentClass> findAllClassesByDepartmentId(Long id, String name) {
        return studentClassRepository.findAllClassesByDepartmentId(id, name);
    }

    @Override
    public List<Department> findAllDeparmentOfUniversity(Long id) {
        return studentClassRepository.findAllDeparmentOfUniversity(id);
    }

    @Override
    public boolean existsByNameAndDepartmentIdAndStatus(String name, Department department) {
        return studentClassRepository.existsByNameAndDepartmentAndStatus(name,department,Status.ACTIVE);
    }

    @Override
    public StudentClass save(StudentClass studentClass) {
        return studentClassRepository.save(studentClass);
    }

    @Override
    @Auditable(action = ActionType.DELETED, entity = Entity.student_class)
    public StudentClass deleteStudentClass(Long id) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        StudentClass studentClass = findById(id);
        studentClass.setStatus(Status.DELETED);
        studentClass.setUpdatedAt(vietnamTime.toLocalDateTime());
        return studentClassRepository.save(studentClass);
    }

    @Override
    public List<StudentClass> searchNameClass(String name) {
        return studentClassRepository.findByNameContainingAndStatus(name, Status.ACTIVE);
    }

    @Override
    public Optional<StudentClass> findByClassNameAndDepartmentId(Long departmentId, String className) {
        return studentClassRepository.findByClassNameAndDepartmentId(departmentId,className);
    }

    @Override
    public boolean existsByIdAndDepartmentId(Long classId, Long departmentId) {
        return studentClassRepository.existsByIdAndDepartmentId(classId,departmentId);
    }

    @Override
    @Auditable(action = ActionType.CREATED, entity = Entity.student_class)
    public StudentClass create(String name, Department department) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        StudentClass studentClass= new StudentClass();
        studentClass.setDepartment(department);
        studentClass.setName(name);
        studentClass.setStatus(Status.ACTIVE);
        studentClass.setCreatedAt(vietnamTime.toLocalDateTime());
        studentClass.setUpdatedAt(vietnamTime.toLocalDateTime());

        return studentClassRepository.save(studentClass);
    }

    @Override
    public StudentClass update(StudentClass studentClass, String name) {
        ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        StudentClass studentClassOld = auditLogService.cloneStudentClass(studentClass);

        studentClass.setName(name);
        studentClass.setUpdatedAt(vietnamTime.toLocalDateTime());
        studentClassRepository.save(studentClass);

        String ipAdress = auditLogService.getClientIp(httpServletRequest);
        List<ActionChange> changes = auditLogService.compareObjects(null, studentClassOld, studentClass);
        if (!changes.isEmpty()) {// nếu khác old mới lưu
            Log log = new Log();
            log.setUser(auditLogService.getCurrentUser());
            log.setActionType(ActionType.UPDATED);
            log.setEntityName(Entity.student_class);
            log.setEntityId(studentClass.getId());
            log.setDescription(LogTemplate.UPDATE_STUDENT_CLASS.getName());
            log.setIpAddress(ipAdress);
            log.setCreatedAt(vietnamTime.toLocalDateTime());

            log = logRepository.save(log);

            for (ActionChange change : changes) {
                change.setLog(log);
            }
            actionChangeRepository.saveAll(changes);
        }
        return studentClass;
    }

    public List<StudentClassResponse> getAllClassofUniversity(Long id, String name){
        List<StudentClass> studentClassList = studentClassRepository.findAllClassOfUniversityByName(id, name);
        List<StudentClassResponse> studentClassResponseList = new ArrayList<>();
        for (StudentClass studentClass : studentClassList){
            StudentClassResponse studentClassResponse = new StudentClassResponse(
                    studentClass.getId(),
                    studentClass.getName()
            );
            studentClassResponseList.add(studentClassResponse);
        }
        return studentClassResponseList;
    }

    // list class của 1 khoa theo id khoa
    public List<StudentClassResponse> listClassOfDepartmentId(Long departmentId, String name){
        List<StudentClass> studentClassList = studentClassRepository.findAllClassesByDepartmentId(departmentId, name);

        List<StudentClassResponse> studentClassResponseList = new ArrayList<>();
        for (StudentClass studentClass : studentClassList){
            StudentClassResponse studentClassResponse = new StudentClassResponse(
                    studentClass.getId(),
                    studentClass.getName()
            );
            studentClassResponseList.add(studentClassResponse);
        }
        return studentClassResponseList;
    }
}
