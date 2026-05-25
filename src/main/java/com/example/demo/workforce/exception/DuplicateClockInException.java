package com.example.demo.workforce.exception;

import org.springframework.http.HttpStatus;

public class DuplicateClockInException extends HrmsException {

    public DuplicateClockInException(String message) {
        super("DUPLICATE_CLOCK_IN", message, HttpStatus.CONFLICT);
    }
}
