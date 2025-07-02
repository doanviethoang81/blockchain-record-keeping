package com.example.blockchain.record.keeping.enums;

public enum LogTemplate {
    IMPORT_DEGREES("Import %d văn bằng từ Excel"),
    REJECTED_DEGREES("Từ chối list %d văn bằng"),
    UPDATE_DEGREES("Cập nhật thông tin văn bằng "),
    VERIFIED_DEGREE("Xác nhận list %d văn bằng"),

    CREATE_STUDENT("Tạo sinh viên"),
    UPDATE_STUDENT("Cập nhật thông tin sinh viên"),
    DELETE_STUDENT("Xoá sinh viên có mã: %s"),
    IMPORT_STUDENT("Import %d sinh viên từ Excel"),

    CREATE_STUDENT_CLASS("Tạo lớp"),
    UPDATE_STUDENT_CLASS("Cập nhật lớp"),
    DELETE_STUDENT_CLASS("Xóa lớp"),

    UPDATE_CERTIFICATE_TYPE("Cập nhật thông tin loại chứng chỉ"),

    CHANGE_PASSWORD_DEPARTMENT("Thay đổi mật khẩu của khoa"),
    UPDATE_DEPARTMENT("Cập nhật thông tin khoa"),

    CREATE_CERTIFICATE("Tạo chứng chỉ cho sinh viên: %s"),
    UPDATE_CERTIFICATE("Cập nhật chứng chỉ"),
    IMPORT_CERTIFICATE("Import %d chứng chỉ từ Excel"),
    REJECTED_CERTIFICATE("Từ chối list %d chứng chỉ"),
    VERIFIED_CERTIFICATE("Xác nhận list %d chứng chỉ");

    private final String template;

    LogTemplate(String template) {
        this.template = template;
    }

    public String format(Object... args) {
        return String.format(template, args);
    }

    public String getName() {
        return String.format(template);
    }
}
