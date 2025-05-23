package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.Degree;
import com.example.blockchain.record.keeping.models.Student;

import java.util.List;

public interface IDegreeService {

    Degree save(Degree degree);

    List<Degree> listDegreeOfStudent(Student student);
}
