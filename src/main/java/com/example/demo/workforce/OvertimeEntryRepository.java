package com.example.demo.workforce;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OvertimeEntryRepository extends JpaRepository<OvertimeEntry, Long> {

    List<OvertimeEntry> findByWorkerIdAndDateBetween(Long workerId, LocalDate from, LocalDate to);

    List<OvertimeEntry> findByWorkerIdAndSettlementStatus(Long workerId, SettlementStatus status);

    @Query("SELECT COALESCE(SUM(o.overtimeHours), 0) FROM OvertimeEntry o WHERE o.worker.id = :workerId AND YEAR(o.date) = :year AND MONTH(o.date) = :month")
    BigDecimal sumOvertimeHoursByWorkerAndMonth(
            @Param("workerId") Long workerId,
            @Param("year") int year,
            @Param("month") int month
    );

    List<OvertimeEntry> findByWorkerIdAndSettlementStatusAndDateBetween(
            Long workerId,
            SettlementStatus status,
            LocalDate from,
            LocalDate to
    );
}
