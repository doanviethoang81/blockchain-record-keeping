package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.dtos.request.DegreeTitleRequest;
import com.example.blockchain.record.keeping.dtos.request.EducationModeRequest;
import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.DegreeTitle;
import com.example.blockchain.record.keeping.models.EducationMode;

import java.util.List;

public interface IDegreeTitleSevice {
    DegreeTitle findByName(String name);
    DegreeTitle findById(Long id);

    List<DegreeTitle> listDegree();

    DegreeTitle update(DegreeTitle degreeTitle, String name);

    DegreeTitle delete(DegreeTitle degreeTitle);

    boolean findByNameAndStatus(String name, Status status);

    DegreeTitle add(DegreeTitleRequest request);

    boolean existsByNameAndStatusAndIdNot(String name, Status status, Long id);

}
