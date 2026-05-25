package com.example.demo.workforce.dto;

public record SiteResponse(
        Long id,
        String siteName,
        String location,
        boolean active
) {
}
