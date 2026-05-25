package com.example.demo.workforce;

import com.example.demo.workforce.dto.ActiveWorkerResponse;
import com.example.demo.workforce.dto.AttendanceLogResponse;
import com.example.demo.workforce.dto.ClockInRequest;
import com.example.demo.workforce.dto.ClockOutRequest;
import com.example.demo.workforce.dto.PagedResponse;
import jakarta.validation.Valid;
import org.springframework.lang.NonNull;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/hrms/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/clock-in")
    public AttendanceLogResponse clockIn(@NonNull @Valid @RequestBody ClockInRequest request) {
        return attendanceService.clockIn(request);
    }

    @PostMapping("/clock-out")
    public AttendanceLogResponse clockOut(@NonNull @Valid @RequestBody ClockOutRequest request) {
        return attendanceService.clockOut(request);
    }

    @GetMapping("/worker/{workerId}")
        public PagedResponse<AttendanceLogResponse> getAttendanceByWorker(
            @PathVariable @NonNull Long workerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return attendanceService.getAttendanceByWorker(workerId, from, to, page, size);
    }

    @GetMapping("/log")
    public PagedResponse<AttendanceLogResponse> getAttendanceLog(
            @RequestParam @NonNull Long workerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return attendanceService.getAttendanceByWorker(workerId, from, to, page, size);
    }

    @GetMapping("/active-workers")
    public List<ActiveWorkerResponse> getActiveWorkers() {
        return attendanceService.getActiveWorkers();
    }
}
