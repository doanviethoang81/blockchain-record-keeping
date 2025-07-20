package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.models.Student;
import com.example.blockchain.record.keeping.models.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet,Long> {

    Optional<Wallet> findByStudent(Student student);

    boolean existsByWalletAddress(String walletAddress);

    @Query(value = """
        SELECT SUM(coin) AS total_coin FROM wallets w
        join students s on s.id = w.student_id
        and s.status NOT LIKE 'DELETED'
    """, nativeQuery = true)
    BigInteger getTotalCoin();

    Wallet findByWalletAddress(String walletAddress);
}
