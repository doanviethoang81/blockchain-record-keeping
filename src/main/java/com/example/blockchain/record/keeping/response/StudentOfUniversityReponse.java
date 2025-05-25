package com.example.blockchain.record.keeping.response;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class StudentOfUniversityReponse {
    private Long id;
    private String name;
    private String className;
    private String departmentName;
    private String studentCode;
    private String email;
    private LocalDate birthDate;
    private String course;
}
