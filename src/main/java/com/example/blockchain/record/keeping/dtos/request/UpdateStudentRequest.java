package com.example.blockchain.record.keeping.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdateStudentRequest {

    @NotBlank(message = "Tên sinh viên không được để trống")
    private String name;

    @NotBlank(message = "Mã sinh viên không được để trống")
    private String studentCode;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotNull(message = "Ngày sinh không được để trống")
    private LocalDate birthDate;

    @NotBlank(message = "Khóa học không được để trống")
    private String course;

    private Long classId;

    private Long departmentId;

    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }
    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode != null ? studentCode.trim() : null;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim() : null;
    }

    public void setCourse(String course) {
        this.course = course != null ? course.trim() : null;
    }
}
