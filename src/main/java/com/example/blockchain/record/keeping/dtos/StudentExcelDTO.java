package com.example.blockchain.record.keeping.dtos;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StudentExcelDTO {

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
    @ColumnWidth(25)
    private String departmentName;

    @ExcelProperty(value= "Email")
    @ColumnWidth(15)
    private String email;

    @ExcelProperty(value= "Ngày sinh")
    @ColumnWidth(15)
    private String birthDate;

    @ExcelProperty("Khóa học")
    @ColumnWidth(13)
    private String course;
}
