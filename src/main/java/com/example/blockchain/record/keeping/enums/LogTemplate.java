package com.example.blockchain.record.keeping.enums;

public enum LogTemplate {
    IMPORT_DEGREES("Tải lên %d văn bằng từ Excel"),
    REJECTED_DEGREES("Từ chối danh sách %d văn bằng"),
    UPDATE_DEGREES("Cập nhật thông tin văn bằng có số hiệu văn bằng: %s"),
    VERIFIED_DEGREE("Xác nhận danh sách %d văn bằng"),

    CREATE_STUDENT("Tạo sinh viên"),
    UPDATE_STUDENT("Cập nhật thông tin sinh viên có mã số: %s"),
    DELETE_STUDENT("Xoá sinh viên có mã số sinh viên: %s"),
    IMPORT_STUDENT("Tải lên %d sinh viên từ Excel"),

    CREATE_STUDENT_CLASS("Tạo lớp"),
    UPDATE_STUDENT_CLASS("Cập nhật thông tin lớp: %s"),
    DELETE_STUDENT_CLASS("Xóa lớp"),

    UPDATE_RATING("Cập nhật xếp loại văn bằng: %s"),

    UPDATE_EDUCATION_MODE("Cập nhật hình thức đào tạo văn bằng: %s"),

    UPDATE_DEGREE_TITLE("Cập nhật thông tin danh hiệu văn bằng: %s"),

    UPDATE_CERTIFICATE_TYPE("Cập nhật thông tin loại chứng chỉ: %s"),

    CHANGE_PASSWORD_DEPARTMENT("Thay đổi mật khẩu của khoa: %s"),
    UPDATE_DEPARTMENT("Cập nhật thông tin khoa: %s"),
    CREATE_DEPARTMENT_EXCEL("Tạo khoa: %s từ file excel"),
    CREATE_CLASS_EXCEL("Tạo lớp: %s từ file excel"),

    UPDATE_UNIVERCITY("Cập nhật thông tin phòng đào tạo"),
    UPDATE_UNIVERCITY_LOGO("Cập nhật logo của trường"),
    UPDATE_UNIVERCITY_SEAL("Cập nhật dấu mộc của trường"),

    EXPORT_EXCEL("Xuất file excel: %s"),
    EXPORT_EXCEL_CERTIFICATE_LIST("Xuất chứng chỉ ra file excel"),
    EXPORT_EXCEL_DEGREE_LIST("Xuất văn bằng ra file excel"),

    CREATE_CERTIFICATE("Tạo chứng chỉ cho sinh viên, số hiệu chứng chỉ: %s"),
    UPDATE_CERTIFICATE("Cập nhật chứng chỉ có số hệu: %s"),
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
