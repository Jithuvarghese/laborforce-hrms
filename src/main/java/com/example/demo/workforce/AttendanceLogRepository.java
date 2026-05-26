package com.example.demo.workforce;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long> {

    Optional<AttendanceLog> findByWorkerIdAndClockOutIsNull(Long workerId);

        @Query(
            value = "SELECT a FROM AttendanceLog a JOIN FETCH a.worker w JOIN FETCH a.site s WHERE a.worker.id = :workerId AND a.date BETWEEN :from AND :to",
            countQuery = "SELECT COUNT(a) FROM AttendanceLog a WHERE a.worker.id = :workerId AND a.date BETWEEN :from AND :to"
        )
    Page<AttendanceLog> findByWorkerIdAndDateRange(
            @Param("workerId") Long workerId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable pageable
    );

    List<AttendanceLog> findByWorkerIdAndDateBetween(Long workerId, LocalDate from, LocalDate to);

    long countByWorkerIdAndDate(Long workerId, LocalDate date);
}
