package com.example.demo.workforce;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SiteRepository extends JpaRepository<Site, Long> {

    Optional<Site> findByIdAndActiveTrue(Long id);

    List<Site> findByActiveTrue();
}
