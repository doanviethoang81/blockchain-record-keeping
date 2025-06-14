package com.example.blockchain.record.keeping.dtos.request;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class DegreeExcelRowRequest {
    @ExcelProperty("Mã số sinh viên")
    private String studentCode;

    @ExcelProperty("Xếp loại")
    private String ratingName;

    @ExcelProperty("Danh hiệu")
    private String degreeTitleName;

    @ExcelProperty("Hình thức đào tạo")
    private String educationModeName;

    @ExcelProperty("Năm tốt nghiệp")
    private String graduationYear;

    @ExcelProperty("Ngày cấp")
    private String issueDate;

    @ExcelProperty("Địa điểm đào tạo")
    private String trainingLocation;

    @ExcelProperty("Người ký")
    private String signer;

    @ExcelProperty("Số hiệu bằng")
    private String diplomaNumber;

    @ExcelProperty("Số vào sổ")
    private String lotteryNumber;
}
