package com.example.demo.workforce.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.lang.NonNull;

public record ClockInRequest(
        @NotNull @NonNull Long workerId,
        @NotNull @NonNull Long siteId
) {
}
