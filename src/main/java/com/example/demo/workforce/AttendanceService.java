package com.example.demo.workforce;

import com.example.demo.workforce.dto.ActiveWorkerResponse;
import com.example.demo.workforce.dto.AttendanceLogResponse;
import com.example.demo.workforce.dto.ClockInRequest;
import com.example.demo.workforce.dto.ClockOutRequest;
import com.example.demo.workforce.dto.PagedResponse;
import com.example.demo.workforce.event.SmsNotificationEvent;
import com.example.demo.workforce.exception.DuplicateClockInException;
import com.example.demo.workforce.exception.SiteNotFoundException;
import com.example.demo.workforce.exception.WorkerNotClockedInException;
import com.example.demo.workforce.exception.WorkerNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceService {

    private static final BigDecimal DAILY_REGULAR_HOURS = BigDecimal.valueOf(8);
    private static final BigDecimal MAX_SHIFT_HOURS = BigDecimal.valueOf(16);

    private final WorkerRepository workerRepository;
    private final SiteRepository siteRepository;
    private final AttendanceLogRepository attendanceLogRepository;
    private final ActiveWorkerCacheService activeWorkerCacheService;
    private final OvertimeService overtimeService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AttendanceService(WorkerRepository workerRepository,
                             SiteRepository siteRepository,
                             AttendanceLogRepository attendanceLogRepository,
                             ActiveWorkerCacheService activeWorkerCacheService,
                             OvertimeService overtimeService,
                             ApplicationEventPublisher applicationEventPublisher) {
        this.workerRepository = workerRepository;
        this.siteRepository = siteRepository;
        this.attendanceLogRepository = attendanceLogRepository;
        this.activeWorkerCacheService = activeWorkerCacheService;
        this.overtimeService = overtimeService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public AttendanceLogResponse clockIn(ClockInRequest request) {
        Worker worker = workerRepository.findByIdAndActiveTrue(request.workerId())
                .orElseThrow(() -> new WorkerNotFoundException(request.workerId()));

        Site site = siteRepository.findByIdAndActiveTrue(request.siteId())
                .orElseThrow(() -> new SiteNotFoundException(request.siteId()));

        boolean activeInCache = activeWorkerCacheService.isWorkerActive(worker.getId());
        boolean activeInDatabase = attendanceLogRepository
                .findByWorkerIdAndClockOutIsNull(worker.getId())
                .isPresent();

        if (activeInCache || activeInDatabase) {
            throw new DuplicateClockInException("Worker already has an active shift: " + worker.getId());
        }

        LocalDateTime now = LocalDateTime.now();
        AttendanceLog attendanceLog = AttendanceLog.builder()
                .worker(worker)
                .site(site)
                .clockIn(now)
                .date(now.toLocalDate())
                .build();

        AttendanceLog saved = attendanceLogRepository.save(attendanceLog);

        activeWorkerCacheService.addActiveWorker(
                worker.getId(),
                worker.getName(),
                site.getId(),
                site.getSiteName(),
                now
        );

        return toResponse(saved);
    }

    @Transactional
    public AttendanceLogResponse clockOut(ClockOutRequest request) {
        Worker worker = workerRepository.findById(request.workerId())
                .orElseThrow(() -> new WorkerNotFoundException(request.workerId()));

        AttendanceLog attendanceLog = attendanceLogRepository.findByWorkerIdAndClockOutIsNull(worker.getId())
                .orElseThrow(() -> new WorkerNotClockedInException(worker.getId()));

        LocalDateTime clockOutTime = LocalDateTime.now();
        BigDecimal totalHours = calculateHours(attendanceLog.getClockIn(), clockOutTime);
        BigDecimal overtimeHours = totalHours.subtract(DAILY_REGULAR_HOURS).max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);
        boolean flagged = totalHours.compareTo(MAX_SHIFT_HOURS) > 0;

        attendanceLog.setClockOut(clockOutTime);
        attendanceLog.setTotalHoursWorked(totalHours);
        attendanceLog.setOvertimeHours(overtimeHours);
        attendanceLog.setFlagged(flagged);

        AttendanceLog saved = attendanceLogRepository.save(attendanceLog);

        if (overtimeHours.compareTo(BigDecimal.ZERO) > 0) {
            overtimeService.calculateAndSaveOvertime(worker, saved, overtimeHours);
        }

        if (flagged) {
            applicationEventPublisher.publishEvent(
                    new SmsNotificationEvent(
                            worker.getPhone(),
                            "Shift alert: your shift exceeded 16 hours on " + saved.getDate()
                    )
            );
        }

        activeWorkerCacheService.removeActiveWorker(worker.getId());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AttendanceLogResponse> getAttendanceByWorker(Long workerId,
                                                                      LocalDate from,
                                                                      LocalDate to,
                                                                      int page,
                                                                      int size) {
        LocalDate start = from != null ? from : LocalDate.now().minusDays(30);
        LocalDate end = to != null ? to : LocalDate.now();

        Pageable pageable = PageRequest.of(page, size);
        Page<AttendanceLog> attendancePage = attendanceLogRepository
                .findByWorkerIdAndDateRange(workerId, start, end, pageable);

        List<AttendanceLogResponse> content = attendancePage.getContent().stream()
                .map(this::toResponse)
                .toList();

        return new PagedResponse<>(
                content,
                attendancePage.getTotalElements(),
                attendancePage.getTotalPages(),
                attendancePage.getNumber(),
                attendancePage.getSize()
        );
    }

    @Transactional(readOnly = true)
    public List<ActiveWorkerResponse> getActiveWorkers() {
        List<Map<String, Object>> payloads = activeWorkerCacheService.getAllActiveWorkers();
        return payloads.stream()
                .map(this::toActiveWorkerResponse)
                .toList();
    }

    private BigDecimal calculateHours(LocalDateTime clockIn, LocalDateTime clockOut) {
        long minutes = Duration.between(clockIn, clockOut).toMinutes();
        return BigDecimal.valueOf(minutes)
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    private AttendanceLogResponse toResponse(AttendanceLog attendanceLog) {
        return new AttendanceLogResponse(
                attendanceLog.getId(),
                attendanceLog.getWorker().getId(),
                attendanceLog.getWorker().getName(),
                attendanceLog.getSite().getId(),
                attendanceLog.getSite().getSiteName(),
                attendanceLog.getClockIn(),
                attendanceLog.getClockOut(),
                attendanceLog.getTotalHoursWorked(),
                attendanceLog.getOvertimeHours(),
                attendanceLog.isFlagged(),
                attendanceLog.getDate()
        );
    }

    private ActiveWorkerResponse toActiveWorkerResponse(Map<String, Object> payload) {
        Long workerId = payload.get("workerId") != null ? Long.parseLong(payload.get("workerId").toString()) : null;
        Long siteId = payload.get("siteId") != null ? Long.parseLong(payload.get("siteId").toString()) : null;

        return new ActiveWorkerResponse(
                workerId,
                (String) payload.get("workerName"),
                siteId,
                (String) payload.get("siteName"),
                payload.get("clockInTime") != null ? payload.get("clockInTime").toString() : null
        );
    }
}
