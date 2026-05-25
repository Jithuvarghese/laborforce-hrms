package com.example.demo.workforce.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public record OvertimeSummaryResponse(
        Long workerId,
        String workerName,
        String month,
        BigDecimal totalOvertimeHours,
        BigDecimal totalPayoutAmount,
        @JsonProperty("settlementStatus")
        String settlementStatus,
        List<OvertimeEntryDetail> breakdown
) {
}
