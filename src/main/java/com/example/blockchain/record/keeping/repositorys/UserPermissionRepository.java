package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.models.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission,Long> {
    List<UserPermission> findByUserId(Long id);
}
