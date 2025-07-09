package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.models.NotificationReceivers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationReceiverRepository extends JpaRepository<NotificationReceivers, Long> {

    @Query(value = """
        SELECT COUNT(*)
        FROM notification_receivers n
        WHERE n.receiver_id = :userId
          AND (:isRead IS NULL OR n.is_read = :isRead)
          AND (:startDate IS NULL OR n.created_at >= :startDate)
          AND (:endDate IS NULL OR n.created_at <= :endDate)
    """,nativeQuery = true)
    long countNotification(@Param("userId") long userId,
                           @Param("isRead") Boolean isRead,
                           @Param("startDate") LocalDateTime startDate,
                           @Param("endDate") LocalDateTime endDate
    );

    @Query(value = """
        SELECT *
        FROM notification_receivers n
        WHERE n.receiver_id = :userId
          AND (:isRead IS NULL OR n.is_read = :isRead)
           AND (:startDate IS NULL OR n.created_at >= :startDate)
          AND (:endDate IS NULL OR n.created_at <= :endDate)
        ORDER BY n.created_at DESC
        LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<NotificationReceivers> notificationList(@Param("userId") long userId,
                                                 @Param("isRead") Boolean isRead,
                                                 @Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate,
                                                 @Param("limit") int limit,
                                                 @Param("offset") int offset
    );

}
