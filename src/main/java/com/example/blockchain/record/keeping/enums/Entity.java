package com.example.blockchain.record.keeping.enums;

public enum Entity {
    certificates("Chứng chỉ"),
    certificate_types("Loại chứng chỉ"),
    degrees("Bằng cấp"),
    departments("Khoa"),
    students("Sinh viên"),
    student_class("Lớp"),
    universitys("Trường"),
    user("Người dùng"),
    rating("Xếp loại"),
    educationMode("Hình thức đào tạo"),
    degreeTitle("Danh hiệu văn bằng");

    private final String name;

    Entity(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
