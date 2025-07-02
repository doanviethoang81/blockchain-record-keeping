package com.example.blockchain.record.keeping.utils;

import com.example.blockchain.record.keeping.enums.ActionType;
import com.example.blockchain.record.keeping.enums.Entity;

import java.util.HashMap;
import java.util.Map;

public class LogMessageTemplate {
    private static final Map<Entity, Map<ActionType, String>> templates = new HashMap<>();

    static {
        Map<ActionType, String> certificateMessages = new HashMap<>();
        certificateMessages.put(ActionType.CREATED, "Tạo chứng chỉ");
        certificateMessages.put(ActionType.UPDATED, "Cập nhật chứng chỉ có");
        certificateMessages.put(ActionType.REJECTED, "Từ chối xác nhận chứng chỉ");
        certificateMessages.put(ActionType.VERIFIED, "Xác thực chứng chỉ");
        templates.put(Entity.certificates, certificateMessages);

        Map<ActionType, String> degreeMessages = new HashMap<>();
        degreeMessages.put(ActionType.CREATED, "Tạo văn bằng");
        degreeMessages.put(ActionType.UPDATED, "Cập nhật văn bằng");
        degreeMessages.put(ActionType.REJECTED, "Từ chối xác nhận văn bằng");
        degreeMessages.put(ActionType.VERIFIED, "Xác thưc văn bằng");
        templates.put(Entity.degrees, degreeMessages);

        Map<ActionType, String> certificateTypeMessages = new HashMap<>();
        certificateTypeMessages.put(ActionType.CREATED, "Tạo loại chứng chỉ");
        certificateTypeMessages.put(ActionType.DELETED, "Xóa loại chứng chỉ");
        templates.put(Entity.certificate_types, certificateTypeMessages);

        Map<ActionType, String> departmentMessages = new HashMap<>();
        departmentMessages.put(ActionType.CREATED, "Tạo khoa");
        departmentMessages.put(ActionType.CHANGE_PASSWORD_DEPARTMENT, "Tạo khoa");
        departmentMessages.put(ActionType.CHANGE_PASSWORD, "Tạo khoa");
        departmentMessages.put(ActionType.DELETED, "Xóa khoa");
        templates.put(Entity.departments, departmentMessages);

        Map<ActionType, String> studentClassMessages = new HashMap<>();
        studentClassMessages.put(ActionType.CREATED, "Tạo lớp");
        studentClassMessages.put(ActionType.DELETED, "Xóa lớp");
        templates.put(Entity.student_class, studentClassMessages);

        Map<ActionType, String> studentMessages = new HashMap<>();
        studentMessages.put(ActionType.CREATED, "Tạo sinh viên");
        studentMessages.put(ActionType.DELETED, "Xóa sinh viên");
        templates.put(Entity.students, studentMessages);

        Map<ActionType, String> universityMessages = new HashMap<>();
        universityMessages.put(ActionType.CREATED, "Tạo trường");// sua
        universityMessages.put(ActionType.DELETED, "Xóa trường");
        templates.put(Entity.universitys, universityMessages);
    }

    public static String getMessage(Entity entity, ActionType actionType) {
        Map<ActionType, String> entityMessages = templates.get(entity);
        if (entityMessages != null && entityMessages.containsKey(actionType)) {
            return entityMessages.get(actionType);
        }
        return actionType.name() + " " + entity;
    }
}
