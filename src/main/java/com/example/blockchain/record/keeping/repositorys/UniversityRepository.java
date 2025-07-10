package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.University;
import com.example.blockchain.record.keeping.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UniversityRepository extends JpaRepository<University, Long> {
    Optional<University> findByEmail(String email);

    //lấy ds các tr từ user
    @Query(value = """
            SELECT u.* FROM users u
            JOIN universitys un on u.university_id = un.id
            JOIN roles r on u.role_id = r.id
            WHERE (:name IS NULL OR un.name LIKE CONCAT('%', :name, '%'))
            AND r.name ='PDT'
            ORDER BY u.updated_at DESC
            """, nativeQuery = true)
    List<User> findAllUserUniversity(@Param("name") String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
