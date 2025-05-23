package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.models.Degree;
import com.example.blockchain.record.keeping.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DegreeRepository extends JpaRepository<Degree,Long> {
    List<Degree> findByStudent(Student student);
}
