package com.example.demo.workforce.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AttendanceLogResponse(
        Long id,
        Long workerId,
        String workerName,
        Long siteId,
        String siteName,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime clockIn,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime clockOut,
        BigDecimal totalHoursWorked,
        BigDecimal overtimeHours,
        boolean flagged,
        LocalDate date
) {
}
