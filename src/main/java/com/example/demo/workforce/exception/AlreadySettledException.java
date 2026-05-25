package com.example.demo.workforce.exception;

import org.springframework.http.HttpStatus;

public class AlreadySettledException extends HrmsException {

    public AlreadySettledException(Long workerId, String month) {
        super("ALREADY_SETTLED", "Overtime already settled for worker " + workerId + " and month " + month, HttpStatus.CONFLICT);
    }
}
