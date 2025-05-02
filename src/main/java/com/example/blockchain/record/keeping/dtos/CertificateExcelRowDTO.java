package com.example.blockchain.record.keeping.dtos;

import com.alibaba.excel.annotation.ExcelProperty;
import com.example.blockchain.record.keeping.excels.LocalDateConverter;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CertificateExcelRowDTO {

    @ExcelProperty("Tên sinh viên")
    private String name;

    @ExcelProperty("Mã số sinh viên")
    private String studentCode;

    @ExcelProperty("Email")
    private String email;

    @ExcelProperty("Lớp")
    private String className;

    @ExcelProperty(value = "Ngày sinh", converter = LocalDateConverter.class)
    private LocalDate dateOfBirth;

    @ExcelProperty("Khóa học")
    private String course;

    @ExcelProperty("Danh hiệu")
    private String degreeTitle;

    @ExcelProperty("Năm tốt nghiệp")
    private String graduationYear;

    @ExcelProperty("Xếp loại")
    private String rating;

    @ExcelProperty(value= "Ngày cấp", converter = LocalDateConverter.class)
    private LocalDate issueDate;

    @ExcelProperty("Hình thức đào tạo")
    private String educationMode;

    @ExcelProperty("Địa điểm đào tạo")
    private String trainingLocation;

    @ExcelProperty("Người ký")
    private String signer;

    @ExcelProperty("Số hiệu bằng")
    private String diplomaNumber;

    @ExcelProperty("Số vào xổ cấp bằng")
    private String lotteryNumber;
}
