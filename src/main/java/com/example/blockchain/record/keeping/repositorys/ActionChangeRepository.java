package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.models.ActionChange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionChangeRepository extends JpaRepository<ActionChange, Long> {

}
