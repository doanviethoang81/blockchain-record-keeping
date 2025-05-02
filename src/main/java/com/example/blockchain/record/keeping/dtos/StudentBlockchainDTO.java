package com.example.blockchain.record.keeping.dtos;

public class StudentBlockchainDTO {
    private String studentCode;
    private String fullName;
    private String dateOfBirth;     // Định dạng dd/MM/yyyy
    private String educationType;
    private String graduationYear;
    private String certificateNumber;
    private String issuedDate;
    private String signer;
}

//struct Student {
//string studentCode;
//string fullName;
//string dateOfBirth;
//string educationType;
//string graduationYear;
//string certificateNumber;
//string issuedDate;
//string signer;
//}
//
//mapping(string => Student) students;
//
//function storeStudent(Student memory s) public {
//    students[s.studentCode] = s;
//}

