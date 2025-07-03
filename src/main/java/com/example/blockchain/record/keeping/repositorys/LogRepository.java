package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.enums.ActionType;
import com.example.blockchain.record.keeping.models.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<Log, Long> {

    //đếm số lượng log của user
    @Query(value = """
        SELECT COUNT(*)
        FROM logs l
        JOIN users u ON l.user_id = u.id
        WHERE u.id = :userId
          AND (:actionType IS NULL OR l.action_type = :actionType)
          AND (:startDate IS NULL OR l.created_at >= :startDate)
          AND (:endDate IS NULL OR l.created_at <= :endDate)
        ORDER BY l.created_at DESC
        """, nativeQuery = true)
    long countLogOfUser(@Param("userId") Long userId,
                        @Param("actionType") String actionType,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate
    );

    //log của 1 university
    @Query(value = """
        SELECT l.*
        FROM logs l
        JOIN users u ON l.user_id = u.id
        WHERE u.id = :userId
          AND (:actionType IS NULL OR l.action_type = :actionType)
          AND (:startDate IS NULL OR l.created_at >= :startDate)
          AND (:endDate IS NULL OR l.created_at <= :endDate)
        ORDER BY l.created_at DESC
        LIMIT :limit OFFSET :offset
        """, nativeQuery = true)
    List<Log> listLogOfUser(@Param("userId") Long userId,
                            @Param("actionType") String actionType,
                            @Param("startDate") LocalDateTime startDate,
                            @Param("endDate") LocalDateTime endDate,
                            @Param("limit") int limit,
                            @Param("offset") int offset
    );

    //đếm số lượng log của các department thuộc uni
    @Query(value = """
        SELECT COUNT(*)
        FROM logs l
        WHERE l.user_id IN (
            SELECT u.id
            FROM users u
            JOIN departments d ON u.department_id = d.id
            WHERE d.university_id = :universityId
              AND d.status = 'ACTIVE'
              AND u.department_id IS NOT NULL
              AND (:userDepartmentId IS NULL OR l.user_id = :userDepartmentId)
              AND (:actionType IS NULL OR l.action_type = :actionType)
              AND (:startDate IS NULL OR l.created_at >= :startDate)
              AND (:endDate IS NULL OR l.created_at <= :endDate)
        )
        """,nativeQuery = true)
    long countLogDepartmentOfUniversity(@Param("universityId") Long universityId,
                                        @Param("userDepartmentId") Long userDepartmentId,
                                        @Param("actionType") String actionType,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate
    );

    //log của các department thuộc university
    @Query(value = """
        SELECT l.*
        FROM logs l
        WHERE l.user_id IN (
            SELECT u.id
            FROM users u
            JOIN departments d ON u.department_id = d.id
            WHERE d.university_id = :universityId
              AND d.status = 'ACTIVE'
              AND u.department_id IS NOT NULL
              AND (:actionType IS NULL OR l.action_type = :actionType)
              AND (:startDate IS NULL OR l.created_at >= :startDate)
              AND (:endDate IS NULL OR l.created_at <= :endDate)
              AND (:userDepartmentId IS NULL OR l.user_id = :userDepartmentId)
        )
        ORDER BY l.created_at DESC
        LIMIT :limit OFFSET :offset
        """,nativeQuery = true)
    List<Log> listLogDepartmentOfUniversity(@Param("universityId") Long universityId,
                                            @Param("userDepartmentId") Long userDepartmentId,
                                            @Param("actionType") String actionType,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate,
                                            @Param("limit") int limit,
                                            @Param("offset") int offset
    );
}
