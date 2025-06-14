package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.DegreeTitle;

import java.util.List;

public interface IDegreeTitleSevice {
    DegreeTitle findByName(String name);
    DegreeTitle findById(Long id);

    List<DegreeTitle> listDegree();
}
