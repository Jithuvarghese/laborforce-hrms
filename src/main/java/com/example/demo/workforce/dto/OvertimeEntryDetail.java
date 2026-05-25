package com.example.demo.workforce.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record OvertimeEntryDetail(
        LocalDate date,
        BigDecimal overtimeHours,
        BigDecimal amount,
        String settlementStatus
) {
}
