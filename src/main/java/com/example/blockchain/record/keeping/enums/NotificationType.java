package com.example.blockchain.record.keeping.enums;

public enum NotificationType {
    CERTIFICATE_PENDING("Tạo chứng chỉ"),
    CERTIFICATE_APPROVED("Xác nhận chứng chỉ"),
    CERTIFICATE_REJECTED("Từ chối chứng chỉ");

    private final String type;

    NotificationType(String type) {
        this.type = type;
    }

    public String getName() {
        return String.format(type);
    }
}
