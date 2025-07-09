package com.example.blockchain.record.keeping.enums;

public enum NotificationType {
    CERTIFICATE_CREATED("Tạo chứng chỉ"),
    CERTIFICATE_APPROVED("Xác nhận chứng chỉ"),
    CERTIFICATE_REJECTED("Từ chối chứng chỉ"),

    DEGREE_CREATED("Tạo văn bằng"),
    DEGREE_APPROVED("Xác nhận văn bằng"),
    DEGREE_REJECTED("Từ chối văn bằng");

    private final String type;

    NotificationType(String type) {
        this.type = type;
    }

    public String getName() {
        return String.format(type);
    }
}
