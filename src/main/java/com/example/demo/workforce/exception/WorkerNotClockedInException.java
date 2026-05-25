package com.example.demo.workforce.exception;

import org.springframework.http.HttpStatus;

public class WorkerNotClockedInException extends HrmsException {

    public WorkerNotClockedInException(Long workerId) {
        super("WORKER_NOT_CLOCKED_IN", "Worker is not currently clocked in: " + workerId, HttpStatus.BAD_REQUEST);
    }
}
