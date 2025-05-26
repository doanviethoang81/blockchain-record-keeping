package com.example.blockchain.record.keeping.dtos.request;

import com.alibaba.excel.annotation.ExcelProperty;
import com.example.blockchain.record.keeping.excels.LocalDateConverter;
import lombok.Data;

import java.time.LocalDate;

@Data
public class StudentExcelRowRequest {

    @ExcelProperty("Tên sinh viên")
    private String name;

    @ExcelProperty("Mã số sinh viên")
    private String studentCode;

    @ExcelProperty("Email")
    private String email;

    @ExcelProperty(value = "Ngày sinh")
    private String dateOfBirth;

    @ExcelProperty("Khóa học")
    private String course;

    @ExcelProperty("Khoa")
    private String departmentName;

    @ExcelProperty("Lớp")
    private String className;
}
