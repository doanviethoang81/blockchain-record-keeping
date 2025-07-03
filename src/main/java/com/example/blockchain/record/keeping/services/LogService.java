package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.Log;
import com.example.blockchain.record.keeping.repositorys.LogRepository;
import com.example.blockchain.record.keeping.response.LogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LogService implements ILogService{

    private final LogRepository logRepository;


    @Override
    public long countLogOfUser(Long userId) {
        return logRepository.countLogOfUser(userId);
    }

    @Override
    public List<LogResponse> listLogOfUser(Long userId, int limit, int offset) {
        List<Log> logList = logRepository.listLogOfUser(userId,limit,offset);
        List<LogResponse> logResponseList = new ArrayList<>();

        for(Log log :logList){
            LogResponse logResponse = new LogResponse(
                    log.getId(),
                    log.getUser().getId(),
                    log.getActionType(),
                    log.getEntityName(),
                    log.getEntityId(),
                    log.getDescription(),
                    log.getIpAddress()
            );
            logResponseList.add(logResponse);
        }
        return logResponseList;
    }

    @Override
    public long countLogDepartmentOfUniversity(Long universityId, Long userDepartmentId) {
        return logRepository.countLogDepartmentOfUniversity(universityId,userDepartmentId);
    }

    @Override
    public List<LogResponse> listLogDepartmentOfUniversity(Long universityId, Long userDepartmentId, int limit, int offset) {
        List<Log> logList = logRepository.listLogDepartmentOfUniversity(universityId, userDepartmentId,limit,offset);
        List<LogResponse> logResponseList = new ArrayList<>();

        for(Log log :logList){
            LogResponse logResponse = new LogResponse(
                    log.getId(),
                    log.getUser().getId(),
                    log.getActionType(),
                    log.getEntityName(),
                    log.getEntityId(),
                    log.getDescription(),
                    log.getIpAddress()
            );
            logResponseList.add(logResponse);
        }
        return logResponseList;
    }
}
