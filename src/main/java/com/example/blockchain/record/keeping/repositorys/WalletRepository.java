package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.models.Student;
import com.example.blockchain.record.keeping.models.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepository extends JpaRepository<Wallet,Long> {

    Wallet findByStudent(Student student);

}
