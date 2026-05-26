package com.example.demo.workforce.dto;

public record ActiveWorkerResponse(
        Long workerId,
        String workerName,
        Long siteId,
        String siteName,
        String clockInTime
) {
}
