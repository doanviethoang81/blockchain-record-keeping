package com.example.blockchain.record.keeping.dtos;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class StudentClassExcelRowDTO {

    @ExcelProperty("Tên lớp")
    private String name;
}
