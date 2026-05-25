package com.example.demo.workforce.dto;

import java.math.BigDecimal;

public record WorkerResponse(
        Long id,
        String name,
        String phone,
        String designation,
        BigDecimal dailyWageRate,
        boolean active
) {
}
