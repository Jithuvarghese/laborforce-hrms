package com.example.demo.workforce.dto;

import jakarta.validation.constraints.NotNull;

public record ClockInRequest(
        @NotNull Long workerId,
        @NotNull Long siteId
) {
}
