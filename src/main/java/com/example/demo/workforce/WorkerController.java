package com.example.demo.workforce;

import com.example.demo.workforce.dto.PagedResponse;
import com.example.demo.workforce.dto.WorkerResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/hrms/workers")
public class WorkerController {

    private final WorkerRepository workerRepository;

    public WorkerController(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
    }

    @GetMapping
    public PagedResponse<WorkerResponse> getActiveWorkers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Worker> workerPage = workerRepository.findByActiveTrue(pageable);

        return new PagedResponse<>(
                workerPage.getContent().stream().map(this::toResponse).toList(),
                workerPage.getTotalElements(),
                workerPage.getTotalPages(),
                workerPage.getNumber(),
                workerPage.getSize()
        );
    }

    @PostMapping
    public WorkerResponse createWorker(@Valid @RequestBody Worker worker) {
        Worker saved = workerRepository.save(worker);
        return toResponse(saved);
    }

    private WorkerResponse toResponse(Worker worker) {
        return new WorkerResponse(
                worker.getId(),
                worker.getName(),
                worker.getPhone(),
                worker.getDesignation().name(),
                worker.getDailyWageRate(),
                worker.isActive()
        );
    }
}
