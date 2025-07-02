package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.enums.Entity;
import com.example.blockchain.record.keeping.models.*;
import com.example.blockchain.record.keeping.repositorys.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final UserRepository userRepository;

    public List<ActionChange> compareObjects(Log log, Object oldObj, Object newObj) {
        List<ActionChange> changes = new ArrayList<>();
        if (oldObj == null || newObj == null) return changes;

        Field[] fields = oldObj.getClass().getDeclaredFields();
        Set<String> excludeFields = Set.of("id", "createdAt", "updatedAt");

        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object oldVal = field.get(oldObj);
                Object newVal = field.get(newObj);

                if (excludeFields.contains(field.getName())) continue;

                String oldDisplay = extractDisplayValue(oldVal);
                String newDisplay = extractDisplayValue(newVal);

                // Chỉ thêm vào khi phần hiển thị khác nhau
                if (oldVal != null && !Objects.equals(oldDisplay, newDisplay)) {
                    ActionChange change = new ActionChange();
                    if (log != null) {
                        change.setLog(log);
                    }
                    change.setFieldName(field.getName());
                    change.setOldValue(oldDisplay);
                    change.setNewValue(newDisplay);
                    changes.add(change);
                }
                if ("studentClass".equals(field.getName())) {// lưu department
                    StudentClass oldClass = (StudentClass) oldVal;
                    StudentClass newClass = (StudentClass) newVal;

                    if (oldClass != null && newClass != null) {
                        Department oldDept = oldClass.getDepartment();
                        Department newDept = newClass.getDepartment();

                        String oldDeptName = oldDept != null ? oldDept.getName() : null;
                        String newDeptName = newDept != null ? newDept.getName() : null;

                        if (!Objects.equals(oldDeptName, newDeptName)) {
                            ActionChange change = new ActionChange();
                            change.setFieldName("department.name");
                            change.setOldValue(oldDeptName);
                            change.setNewValue(newDeptName);
                            if (log != null) change.setLog(log);
                            changes.add(change);
                        }
                    }
                }
            } catch (IllegalAccessException ignored) {}
        }
        return changes;
    }

    //kiem tra xem trường là eintity hay string, enum
    private String extractDisplayValue(Object value) {
        if (value == null) return null;

        // Trường hợp enum
        if (value instanceof Enum<?> enumVal) {
            try {
                Method getLabel = value.getClass().getMethod("getLabel");
                Object label = getLabel.invoke(value);
                return label != null ? label.toString() : enumVal.name();
            } catch (Exception e) {
                return enumVal.name();
            }
        }

        // Trường hợp entity có getName()
        try {
            Method getName = value.getClass().getMethod("getName");
            Object name = getName.invoke(value);
            return name != null ? name.toString() : value.toString();
        } catch (NoSuchMethodException ignored) {
            // Không có getName → thử getId
        } catch (Exception e) {
            return value.toString();
        }

        // Nếu không có getName, thử getId
        try {
            Method getId = value.getClass().getMethod("getId");
            Object id = getId.invoke(value);
            return id != null ? "ID:" + id.toString() : value.toString();
        } catch (Exception ignored) {}

        // Cuối cùng fallback
        return value.toString();
    }

    public Degree cloneDegree(Degree source) {
        Degree clone = new Degree();
        clone.setRating(source.getRating());
        clone.setDegreeTitle(source.getDegreeTitle());
        clone.setEducationMode(source.getEducationMode());
        clone.setGraduationYear(source.getGraduationYear());
        clone.setIssueDate(source.getIssueDate());
        clone.setTrainingLocation(source.getTrainingLocation());
        clone.setSigner(source.getSigner());
        clone.setDiplomaNumber(source.getDiplomaNumber());
        clone.setLotteryNumber(source.getLotteryNumber());
        clone.setImageUrl(source.getImageUrl());
        return clone;
    }

    public Certificate cloneCertificate(Certificate source) {
        Certificate clone = new Certificate();
        clone.setUniversityCertificateType(source.getUniversityCertificateType());
        clone.setIssueDate(source.getIssueDate());
        clone.setDiplomaNumber(source.getDiplomaNumber());
        clone.setSigner(source.getSigner());
        clone.setGrantor(source.getGrantor());
        clone.setImageUrl(source.getImageUrl());
        return clone;
    }

    public Department cloneDepartment(Department source) {
        Department clone = new Department();
        clone.setName(source.getName());
        clone.setStatus(source.getStatus());
        return clone;
    }

    public Student cloneStudent(Student source) {
        Student clone = new Student();
        clone.setName(source.getName());
        clone.setStudentCode(source.getStudentCode());
        clone.setEmail(source.getEmail());
        clone.setBirthDate(source.getBirthDate());
        clone.setCourse(source.getCourse());
        // clone lớp
        if (source.getStudentClass() != null) {
            StudentClass clazz = new StudentClass();
            clazz.setId(source.getStudentClass().getId());
            clazz.setName(source.getStudentClass().getName());

            // clone khoa
            if (source.getStudentClass().getDepartment() != null) {
                Department dept = new Department();
                dept.setId(source.getStudentClass().getDepartment().getId());
                dept.setName(source.getStudentClass().getDepartment().getName());
                clazz.setDepartment(dept);
            }

            clone.setStudentClass(clazz);
        }

        return clone;
    }

    public StudentClass cloneStudentClass(StudentClass source) {
        StudentClass clone = new StudentClass();
        clone.setName(source.getName());
        clone.setStatus(source.getStatus());
        return clone;
    }

    public User cloneUser(User source) {
        User clone = new User();
        clone.setEmail(source.getEmail());
        clone.setPassword(source.getPassword());
        clone.setLocked(source.isLocked());
        return clone;
    }

    public User cloneUser(Optional<User> source) {
        User clone = new User();
        clone.setPassword(source.get().getPassword());
        clone.setEmail(source.get().getEmail());
        clone.setLocked(source.get().isLocked());
        return clone;
    }

    public CertificateType cloneCertificateType(CertificateType source) {
        CertificateType clone = new CertificateType();
        clone.setName(source.getName());
        return clone;
    }


    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByEmail(username).orElse(null);
        return user;
    }

    public String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        return xfHeader != null ? xfHeader.split(",")[0] : request.getRemoteAddr();
    }
}
