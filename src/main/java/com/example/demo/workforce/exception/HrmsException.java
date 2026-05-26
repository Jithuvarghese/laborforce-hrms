package com.example.demo.workforce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

public class HrmsException extends RuntimeException {

    private final String errorCode;
    private final @NonNull HttpStatus status;

    public HrmsException(String errorCode, String message, @NonNull HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public @NonNull HttpStatus getStatus() {
        return status;
    }
}
