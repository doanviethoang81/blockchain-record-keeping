package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.DegreeTitle;

public interface IDegreeTitleSevice {
    DegreeTitle findByName(String name);

}
