package com.example.demo.workforce.exception;

import org.springframework.http.HttpStatus;

public class SiteNotFoundException extends HrmsException {

    public SiteNotFoundException(Long siteId) {
        super("SITE_NOT_FOUND", "Site not found or inactive: " + siteId, HttpStatus.NOT_FOUND);
    }
}
