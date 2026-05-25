package com.example.demo.workforce;

import com.example.demo.workforce.dto.OvertimeEntryDetail;
import com.example.demo.workforce.dto.OvertimeSummaryResponse;
import com.example.demo.workforce.event.SmsNotificationEvent;
import com.example.demo.workforce.exception.AlreadySettledException;
import com.example.demo.workforce.exception.CannotSettleCurrentMonthException;
import com.example.demo.workforce.exception.WorkerNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Objects;
import org.springframework.lang.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@SuppressWarnings("null")
public class OvertimeService {

    private static final BigDecimal MONTHLY_OVERTIME_CAP = BigDecimal.valueOf(60);
    private static final BigDecimal TIER_ONE_LIMIT = BigDecimal.valueOf(2);

    private final OvertimeEntryRepository overtimeEntryRepository;
    private final WorkerRepository workerRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public OvertimeService(OvertimeEntryRepository overtimeEntryRepository,
                           WorkerRepository workerRepository,
                           ApplicationEventPublisher applicationEventPublisher) {
        this.overtimeEntryRepository = overtimeEntryRepository;
        this.workerRepository = workerRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
        public OvertimeEntry calculateAndSaveOvertime(@NonNull Worker worker,
                                                                                                  @NonNull AttendanceLog attendanceLog,
                                                                                                  @NonNull BigDecimal overtimeHours) {
        YearMonth month = YearMonth.from(attendanceLog.getDate());
        BigDecimal existingMonthHours = overtimeEntryRepository.sumOvertimeHoursByWorkerAndMonth(
                worker.getId(),
                month.getYear(),
                month.getMonthValue()
        );

        BigDecimal allowedHours = MONTHLY_OVERTIME_CAP.subtract(existingMonthHours).max(BigDecimal.ZERO);
        BigDecimal billableOvertimeHours = overtimeHours.min(allowedHours).setScale(2, RoundingMode.HALF_UP);

        if (billableOvertimeHours.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        BigDecimal baseHourlyRate = worker.getDailyWageRate()
                .divide(BigDecimal.valueOf(8), 4, RoundingMode.HALF_UP);

        BigDecimal amount = calculateTieredAmount(baseHourlyRate, billableOvertimeHours);
        BigDecimal effectiveRate = amount.divide(billableOvertimeHours, 4, RoundingMode.HALF_UP);

        OvertimeEntry entry = OvertimeEntry.builder()
                .worker(worker)
                .attendanceLog(attendanceLog)
                .date(attendanceLog.getDate())
                .overtimeHours(billableOvertimeHours)
                .overtimeRate(effectiveRate)
                .amount(amount)
                .settlementStatus(SettlementStatus.PENDING)
                .build();

                return Objects.requireNonNull(overtimeEntryRepository.save(entry));
    }

    @Transactional(readOnly = true)
        public OvertimeSummaryResponse getMonthlySummary(@NonNull Long workerId, @NonNull YearMonth month) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new WorkerNotFoundException(workerId));

        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();

        List<OvertimeEntry> entries = overtimeEntryRepository.findByWorkerIdAndDateBetween(workerId, from, to);

        BigDecimal totalOvertimeHours = entries.stream()
                .map(OvertimeEntry::getOvertimeHours)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalPayout = entries.stream()
                .map(OvertimeEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        String settlementStatus = resolveSettlementStatus(entries);

        List<OvertimeEntryDetail> details = entries.stream()
                .map(entry -> new OvertimeEntryDetail(
                        entry.getDate(),
                        entry.getOvertimeHours(),
                        entry.getAmount(),
                        entry.getSettlementStatus().name()
                ))
                .toList();

        return new OvertimeSummaryResponse(
                worker.getId(),
                worker.getName(),
                month.toString(),
                totalOvertimeHours,
                totalPayout,
                settlementStatus,
                details
        );
    }

    @Transactional
        public OvertimeSummaryResponse settleMonthlyOvertime(@NonNull Long workerId, @NonNull YearMonth month) {
        if (YearMonth.now().equals(month)) {
            throw new CannotSettleCurrentMonthException(month.toString());
        }

        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new WorkerNotFoundException(workerId));

        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();

        List<OvertimeEntry> pendingEntries = overtimeEntryRepository
                .findByWorkerIdAndSettlementStatusAndDateBetween(workerId, SettlementStatus.PENDING, from, to);

        if (pendingEntries.isEmpty()) {
            throw new AlreadySettledException(workerId, month.toString());
        }

        LocalDateTime settledAt = LocalDateTime.now();
        pendingEntries.forEach(entry -> {
            entry.setSettlementStatus(SettlementStatus.SETTLED);
            entry.setSettledAt(settledAt);
        });

        overtimeEntryRepository.saveAll(pendingEntries);

        BigDecimal settledAmount = pendingEntries.stream()
                .map(OvertimeEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        applicationEventPublisher.publishEvent(
                new SmsNotificationEvent(
                        worker.getPhone(),
                        "Overtime settled for " + month + ". Total payout: " + settledAmount
                )
        );

        return getMonthlySummary(workerId, month);
    }

    private BigDecimal calculateTieredAmount(BigDecimal baseHourlyRate, BigDecimal overtimeHours) {
        BigDecimal firstTierHours = overtimeHours.min(TIER_ONE_LIMIT);

        BigDecimal secondTierHours = overtimeHours.subtract(TIER_ONE_LIMIT).max(BigDecimal.ZERO);

        BigDecimal firstTierAmount = firstTierHours
                .multiply(baseHourlyRate)
                .multiply(BigDecimal.valueOf(1.50));

        BigDecimal secondTierAmount = secondTierHours
                .multiply(baseHourlyRate)
                .multiply(BigDecimal.valueOf(2.00));

        return firstTierAmount
                .add(secondTierAmount)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String resolveSettlementStatus(List<OvertimeEntry> entries) {
        if (entries.isEmpty()) {
            return "NONE";
        }

        boolean allSettled = entries.stream().allMatch(entry -> entry.getSettlementStatus() == SettlementStatus.SETTLED);
        return allSettled ? SettlementStatus.SETTLED.name() : SettlementStatus.PENDING.name();
    }
}
