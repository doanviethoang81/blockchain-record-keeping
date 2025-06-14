package com.example.blockchain.record.keeping.repositorys;

import com.example.blockchain.record.keeping.enums.Status;
import com.example.blockchain.record.keeping.models.Degree;
import com.example.blockchain.record.keeping.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface DegreeRepository extends JpaRepository<Degree,Long> {
    boolean existsByStudentAndStatusNot(Student student, Status status);
    boolean existsByIdAndStatus(Long id, Status status);

    @Query(value = """
    SELECT s.student_code
    FROM degrees d
    JOIN students s ON d.student_id = s.id
    WHERE s.student_code IN :studentCodes
""", nativeQuery = true)
    List<String> findStudentCodesWithDegree(@Param("studentCodes") Set<String> studentCodes);

    Optional<Degree> findByDiplomaNumber(String diplomaNumber);
    Optional<Degree> findByLotteryNumber(String lotteryNumber);

    @Query(value = """
            SELECT d.diploma_number
            FROM degrees d
            WHERE d.diploma_number IN :diplomaNumbers
            """,nativeQuery = true)
    List<String> findExistingDiplomaNumbers(@Param("diplomaNumbers") Collection<String> diplomaNumbers);

    @Query(value = """
            SELECT d.lottery_number
            FROM degrees d
            WHERE d.lottery_number IN :lotteryNumbers
            """,nativeQuery = true)
    List<String> findExistingLotteryNumbers(@Param("lotteryNumbers") Collection<String> lotteryNumbers);
}
