package com.example.blockchain.record.keeping.enums;

public enum ActionType {
    CREATED("Tạo"),
    UPDATED("Cập nhật"),
    DELETED("Xóa"),
    REJECTED("Từ chối"),
    CHANGE_PASSWORD("Thay đổi mật khẩu"),
    CHANGE_PASSWORD_DEPARTMENT("Thay đổi mật khẩu của khoa"),
    LOCKED("Khóa tài khoản"),
    UNLOCKED("Mở khóa tài khoản"),
    LOCK_READ("Khóa quyền read của"),
    UNLOCK_READ("Mở khóa quyền đọc của"),
    LOCK_WRITE("Khóa quyền write của"),
    UNLOCK_WRITE("Mở khóa quyền write"),
    VERIFIED("Xác thực");


    private final String label;

    ActionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
