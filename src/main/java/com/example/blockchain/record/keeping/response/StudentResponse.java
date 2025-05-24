package com.example.blockchain.record.keeping.response;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class StudentResponse {

    private String name;
    private String studentCode;
    private String email;
    private String className;
    private LocalDate birthDate;
    private String course;

}
