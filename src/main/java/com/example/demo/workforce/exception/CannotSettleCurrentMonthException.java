package com.example.demo.workforce.exception;

import org.springframework.http.HttpStatus;

public class CannotSettleCurrentMonthException extends HrmsException {

    public CannotSettleCurrentMonthException(String month) {
        super("CANNOT_SETTLE_CURRENT_MONTH", "Cannot settle overtime for current month: " + month, HttpStatus.BAD_REQUEST);
    }
}
