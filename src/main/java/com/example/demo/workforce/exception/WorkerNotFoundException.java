package com.example.demo.workforce.exception;

import org.springframework.http.HttpStatus;

public class WorkerNotFoundException extends HrmsException {

    public WorkerNotFoundException(Long workerId) {
        super("WORKER_NOT_FOUND", "Worker not found or inactive: " + workerId, HttpStatus.NOT_FOUND);
    }
}
