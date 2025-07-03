package com.example.blockchain.record.keeping.enums;

public enum LogTemplate {
    IMPORT_DEGREES("Tải lên %d văn bằng từ Excel"),
    REJECTED_DEGREES("Từ chối danh sách %d văn bằng"),
    UPDATE_DEGREES("Cập nhật thông tin văn bằng có số hiệu văn bằng: %d"),
    VERIFIED_DEGREE("Xác nhận danh sách %d văn bằng"),

    CREATE_STUDENT("Tạo sinh viên"),
    UPDATE_STUDENT("Cập nhật thông tin sinh viên có mã số: %d"),
    DELETE_STUDENT("Xoá sinh viên có mã số sinh viên: %s"),
    IMPORT_STUDENT("Tải lên %d sinh viên từ Excel"),

    CREATE_STUDENT_CLASS("Tạo lớp"),
    UPDATE_STUDENT_CLASS("Cập nhật thông tin lớp: %d"),
    DELETE_STUDENT_CLASS("Xóa lớp"),

    UPDATE_CERTIFICATE_TYPE("Cập nhật thông tin loại chứng chỉ: %d"),

    CHANGE_PASSWORD_DEPARTMENT("Thay đổi mật khẩu của khoa: %d"),
    UPDATE_DEPARTMENT("Cập nhật thông tin khoa: %d"),

    CREATE_CERTIFICATE("Tạo chứng chỉ cho sinh viên, số hiệu chứng chỉ: %s"),
    UPDATE_CERTIFICATE("Cập nhật chứng chỉ có số hệu: %d"),
    IMPORT_CERTIFICATE("Tải lên %d chứng chỉ từ Excel"),
    REJECTED_CERTIFICATE("Từ chối danh sách %d chứng chỉ"),
    VERIFIED_CERTIFICATE("Xác nhận danh sách %d chứng chỉ");

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
