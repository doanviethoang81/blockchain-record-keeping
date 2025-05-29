package com.example.blockchain.record.keeping.dtos;

import com.alibaba.excel.annotation.ExcelProperty;
import com.example.blockchain.record.keeping.excels.LocalDateConverter;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CertificateExcelRowDTO {

    @ExcelProperty("Mã số sinh viên")
    private String studentCode;

    @ExcelProperty(value= "Ngày cấp")
    private String issueDate;

    @ExcelProperty("Chức vụ người cấp")
    private String grantor;

    @ExcelProperty("Người ký")
    private String signer;

    @ExcelProperty("Số hiệu bằng")
    private String diplomaNumber;
}
