package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.models.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission,Long> {
}
