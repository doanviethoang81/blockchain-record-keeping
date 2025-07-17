package com.example.blockchain.record.keeping.dtos;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class DepartmentExcelRowDTO {

    @ExcelProperty("Tên khoa")
    private String name;

    @ExcelProperty("Email")
    private String email;

    @ExcelProperty("Mật khẩu")
    private String password;
}
