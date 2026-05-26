package com.example.demo.workforce;

import com.example.demo.workforce.dto.PagedResponse;
import com.example.demo.workforce.dto.SiteResponse;
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
@RequestMapping({"/api/v1/hrms/sites", "/api/sites"})
public class SiteController {

    private final SiteRepository siteRepository;

    public SiteController(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @GetMapping
    public PagedResponse<SiteResponse> getActiveSites(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Site> sitePage = siteRepository.findByActiveTrue(pageable);

        return new PagedResponse<>(
                sitePage.getContent().stream().map(this::toResponse).toList(),
                sitePage.getTotalElements(),
                sitePage.getTotalPages(),
                sitePage.getNumber(),
                sitePage.getSize()
        );
    }

    @PostMapping
    public SiteResponse createSite(@Valid @RequestBody Site site) {
        Site saved = siteRepository.save(site);
        return toResponse(saved);
    }

    private SiteResponse toResponse(Site site) {
        return new SiteResponse(
                site.getId(),
                site.getSiteName(),
                site.getLocation(),
                site.isActive()
        );
    }
}
