package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.Log;
import com.example.blockchain.record.keeping.response.LogResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface ILogService {
    long countLogOfUser(Long userId,String actionType, LocalDateTime startDate, LocalDateTime endDate);

    List<LogResponse> listLogOfUser(Long userId, String actionType, LocalDateTime startDate, LocalDateTime endDate, int limit, int offset);

    long countLogDepartmentOfUniversity(Long universityId, Long userDepartmentId,String actionType, LocalDateTime startDate, LocalDateTime endDate);

    List<LogResponse> listLogDepartmentOfUniversity(Long universityId, Long userDepartmentId,String actionType, LocalDateTime startDate, LocalDateTime endDate, int limit, int offset);

}
