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
public class DegreeExcelDTO {

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

    @ExcelProperty(value= "Năm tốt nghiệp")
    @ColumnWidth(10)
    private String graduationYear;

    @ExcelProperty("Xếp loại")
    @ColumnWidth(13)
    private String rating;

    @ExcelProperty("Danh hiệu")
    @ColumnWidth(13)
    private String degreeTitle;

    @ExcelProperty("Hình thức đào tạo")
    @ColumnWidth(19)
    private String educationMode;

    @ExcelProperty("Người ký")
    @ColumnWidth(21)
    private String signer;

    @ExcelProperty("Số hiệu bằng")
    @ColumnWidth(16)
    private String diplomaNumber;

    @ExcelProperty("Số vào sổ")
    @ColumnWidth(16)
    private String lotteryNumber;

    @ExcelProperty("Trạng thái")
    @ColumnWidth(12)
    private String status;
}
