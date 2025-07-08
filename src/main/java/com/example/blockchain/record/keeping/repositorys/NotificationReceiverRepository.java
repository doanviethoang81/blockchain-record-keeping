package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.models.NotificationReceivers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationReceiverRepository extends JpaRepository<NotificationReceivers, Long> {
}
