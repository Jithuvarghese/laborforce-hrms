package com.example.demo.workforce.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.lang.NonNull;

public record ClockOutRequest(
        @NotNull @NonNull Long workerId
) {
}
