package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.Log;
import com.example.blockchain.record.keeping.response.LogResponse;

import java.util.List;

public interface ILogService {
    long countLogOfUser(Long userId);

    List<LogResponse> listLogOfUser(Long userId, int limit, int offset);

    long countLogDepartmentOfUniversity(Long universityId, Long userDepartmentId);

    List<LogResponse> listLogDepartmentOfUniversity(Long universityId, Long userDepartmentId,int limit, int offset);

}
