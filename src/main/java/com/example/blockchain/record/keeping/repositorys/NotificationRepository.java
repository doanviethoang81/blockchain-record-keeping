package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.models.Notifications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notifications, Long> {

}
