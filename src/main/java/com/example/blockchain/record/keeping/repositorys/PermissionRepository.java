package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.models.Department;
import com.example.blockchain.record.keeping.models.Permission;
import com.example.blockchain.record.keeping.models.University;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission,Long> {

    Optional<Permission> findByAction(String action);

    @Query(value = """
    SELECT EXISTS (
            SELECT 1
                    FROM user_permissions up
                    JOIN users u ON up.user_id = u.id
                    JOIN permissions p ON up.permission_id = p.id
                    WHERE u.email = :username
                    AND p.action = :permissionAction
    );
    """,nativeQuery = true)
    Integer existsByUsernameAndPermission(String username, String permissionAction);
}

