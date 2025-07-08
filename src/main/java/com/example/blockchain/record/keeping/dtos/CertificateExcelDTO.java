package com.example.blockchain.record.keeping.dtos;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.example.blockchain.record.keeping.enums.Status;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CertificateExcelDTO {

    @ExcelProperty("STT")
    @ColumnWidth(5)
    private Integer stt;

    @ExcelProperty("Mã số sinh viên")
    @ColumnWidth(15)
    private String studentCode;

    @ExcelProperty("Tên sinh viên")
    @ColumnWidth(25)
    private String studentName;

    @ExcelProperty("Lớp")
    @ColumnWidth(12)
    private String studentClass;

    @ExcelProperty("Tên khoa")
    @ColumnWidth(22)
    private String departmentName;

    @ExcelProperty(value= "Ngày cấp")
    @ColumnWidth(15)
    private LocalDate issueDate;

    @ExcelProperty("Chức vụ người cấp")
    @ColumnWidth(20)
    private String grantor;

    @ExcelProperty("Người ký")
    @ColumnWidth(25)
    private String signer;

    @ExcelProperty("Số hiệu bằng")
    @ColumnWidth(13)
    private String diplomaNumber;

    @ExcelProperty("Trạng thái")
    @ColumnWidth(12)
    private String status;
}
