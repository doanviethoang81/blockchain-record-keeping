package com.example.blockchain.record.keeping.response;

import com.example.blockchain.record.keeping.dtos.CertificateDTO;
import com.example.blockchain.record.keeping.dtos.DegreeDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class StudentDetailReponse {

    private String name;
    private String studentCode;
    private String email;
    private LocalDate birthDate;
    private String course;
    private List<DegreeDTO> degrees;
    private List<CertificateDTO> certificates;
}
