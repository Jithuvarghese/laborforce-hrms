package com.example.demo.workforce.dto;

import jakarta.validation.constraints.NotNull;

public record ClockOutRequest(
        @NotNull Long workerId
) {
}
