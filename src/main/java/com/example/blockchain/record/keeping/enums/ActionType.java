package com.example.blockchain.record.keeping.enums;

public enum ActionType {
    CREATED("Tạo"),
    UPDATED("Cập nhật"),
    DELETED("Xóa"),
    REJECTED("Từ chối"),
    CHANGE_PASSWORD("Thay đổi mật khẩu"),
    CHANGE_PASSWORD_DEPARTMENT("Thay đổi mật khẩu của khoa"),
    LOCKED("Khóa tài khoản khoa: %d"),
    UNLOCKED("Mở khóa tài khoản khoa:"),
    LOCK_READ("Khóa quyền read của"),
    UNLOCK_READ("Mở khóa quyền đọc của"),
    LOCK_WRITE("Khóa quyền write của"),
    UNLOCK_WRITE("Mở khóa quyền write"),
    LOCK_DEPARTMENT("Khóa tài khoản khoa"),
    UNLOCK_DEPARTMENT("Mở khóa tài khoản khoa"),
    EXPORT_EXCEL("Xuất file excel"),
    COIN("Coin"),
    VERIFIED("Xác thực");


    private final String label;

    ActionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
