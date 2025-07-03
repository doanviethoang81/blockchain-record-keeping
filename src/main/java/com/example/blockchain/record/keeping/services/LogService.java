package com.example.blockchain.record.keeping.services;

import com.example.blockchain.record.keeping.models.ActionChange;
import com.example.blockchain.record.keeping.models.Log;
import com.example.blockchain.record.keeping.repositorys.LogRepository;
import com.example.blockchain.record.keeping.response.ActionChangeResponse;
import com.example.blockchain.record.keeping.response.LogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LogService implements ILogService{

    private final LogRepository logRepository;
    private final ActionChangeService actionChangeService;


    @Override
    public long countLogOfUser(Long userId, String actionType, LocalDateTime startDate, LocalDateTime endDate) {
        return logRepository.countLogOfUser(userId, actionType,startDate,endDate);
    }

    @Override
    public List<LogResponse> listLogOfUser(Long userId, String actionType, LocalDateTime startDate, LocalDateTime endDate, int limit, int offset) {
        List<Log> logList = logRepository.listLogOfUser(userId,actionType,startDate,endDate, limit,offset);
        List<LogResponse> logResponseList = new ArrayList<>();

        for(Log log :logList){
            List<ActionChangeResponse> actionChangeList = actionChangeService.getActionChange(log.getId());
            LogResponse logResponse = new LogResponse(
                    log.getId(),
                    log.getUser().getId(),
                    log.getActionType(),
                    log.getEntityName(),
                    log.getEntityId(),
                    log.getDescription(),
                    log.getIpAddress(),
                    actionChangeList
            );
            logResponseList.add(logResponse);
        }
        return logResponseList;
    }

    @Override
    public long countLogDepartmentOfUniversity(Long universityId, Long userDepartmentId,String actionType, LocalDateTime startDate, LocalDateTime endDate) {
        return logRepository.countLogDepartmentOfUniversity(universityId,userDepartmentId, actionType, startDate,endDate);
    }

    @Override
    public List<LogResponse> listLogDepartmentOfUniversity(Long universityId, Long userDepartmentId,String actionType, LocalDateTime startDate, LocalDateTime endDate, int limit, int offset) {
        List<Log> logList = logRepository.listLogDepartmentOfUniversity(universityId, userDepartmentId,actionType,startDate,endDate, limit,offset);
        List<LogResponse> logResponseList = new ArrayList<>();

        for(Log log :logList){
            List<ActionChangeResponse> actionChangeList = actionChangeService.getActionChange(log.getId());
            LogResponse logResponse = new LogResponse(
                    log.getId(),
                    log.getUser().getId(),
                    log.getActionType(),
                    log.getEntityName(),
                    log.getEntityId(),
                    log.getDescription(),
                    log.getIpAddress(),
                    actionChangeList
            );
            logResponseList.add(logResponse);
        }
        return logResponseList;
    }
}
