package com.example.blockchain.record.keeping.enums;

public enum Status {
    ACTIVE("Đang hoạt động"),
    DELETED("Đã xoá"),
    PENDING("Chưa duyệt"),
    APPROVED("Đã duyệt"),
    REJECTED("Đã từ chối");

    private final String label;

    Status(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

