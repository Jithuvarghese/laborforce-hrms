package com.example.demo.workforce;

import com.example.demo.workforce.dto.OvertimeSummaryResponse;
import java.time.YearMonth;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/hrms/overtime")
public class OvertimeController {

    private final OvertimeService overtimeService;

    public OvertimeController(OvertimeService overtimeService) {
        this.overtimeService = overtimeService;
    }

    @GetMapping("/summary/{workerId}")
    public OvertimeSummaryResponse getMonthlySummary(
            @PathVariable Long workerId,
            @RequestParam String month
    ) {
        return overtimeService.getMonthlySummary(workerId, YearMonth.parse(month));
    }

    @PostMapping("/settle/{workerId}")
    public OvertimeSummaryResponse settleMonthlyOvertime(
            @PathVariable Long workerId,
            @RequestParam String month
    ) {
        return overtimeService.settleMonthlyOvertime(workerId, YearMonth.parse(month));
    }
}
