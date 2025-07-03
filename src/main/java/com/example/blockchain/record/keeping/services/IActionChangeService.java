package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.response.ActionChangeResponse;

import java.util.List;

public interface IActionChangeService {

    List<ActionChangeResponse> getActionChange(Long id);
}
