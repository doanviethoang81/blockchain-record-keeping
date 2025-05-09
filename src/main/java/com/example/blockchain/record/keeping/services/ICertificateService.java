package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.CertificateDTO;
import com.example.blockchain.record.keeping.dtos.StudentDTO;
import com.example.blockchain.record.keeping.models.Certificate;
import com.example.blockchain.record.keeping.models.Student;

public interface ICertificateService {

    Certificate saveAll(CertificateDTO certificateDTO, StudentDTO studentDTO);
}
